package nomad.searchspace.domain.post.service;

import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.Review.entity.Review;
import nomad.searchspace.domain.Review.entity.ReviewImage;
import nomad.searchspace.domain.Review.repository.ReviewImageRepository;
import nomad.searchspace.domain.Review.repository.ReviewRepository;
import nomad.searchspace.domain.like.repository.LikeRepository;
import nomad.searchspace.domain.like.service.LikeRankingService;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.domain.member.service.RedisService;
import nomad.searchspace.domain.post.DTO.*;
import nomad.searchspace.domain.post.entity.Post;
import nomad.searchspace.domain.post.entity.PostImage;
import nomad.searchspace.domain.post.repository.PostImageRepository;
import nomad.searchspace.domain.post.repository.PostRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;


import nomad.searchspace.global.service.S3Service;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "posts")
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper mapper;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final PostImageRepository postImageRepository;
    private final LikeRankingService likeRankingService;

    @Value("${KAKAO_CLIENT}")
    private String KAKAO_CLIENT;


    //게시물 생성 요청
    public PostResponse create(PostDTO dto, List<MultipartFile> images, PrincipalDetails principalDetails) throws IOException, ParseException {
        //회원정보가 없을시 예외 반환
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        
        Post post = mapper.toEntity(dto);

        // 주소에서 위도와 경도 가져오기
        double[] GEOCode;
        try {
            GEOCode = getGEOCode(post.getAddress());
        } catch (ApiException e) {
            // 외부 API 또는 잘못된 주소로 인한 예외 처리
            throw e;
        }
        post.setLatitude(GEOCode[0]);
        post.setLongitude(GEOCode[1]);
        post.setMember(member);
        post = postRepository.save(post);

        // 이미지 업로드
        List<PostImage> postImages = new ArrayList<>();
        int count = 1;
        for (MultipartFile image : images) {
            String imageUrl = s3Service.upload(image);

            PostImage postImage = PostImage.builder()
                    .imageUrl(imageUrl)
                    .description(post.getTitle() + "사진" + (count++))
                    .post(post)
                    .build();
            postImages.add(postImage);
        }

        postImageRepository.saveAll(postImages);

        post.setImages(postImages);
        
        return mapper.toResponse(post, false);
    }

    //특정 id로 상세정보 가져오기
    public PostResponse getPost(Long postId, PrincipalDetails principalDetails) {
        Post post = postRepository.findById(postId).orElseThrow(()->new ApiException(ErrorCode.SPACE_NOT_FOUND));
        boolean userLiked=false;

        // PrincipalDetails가 null인지 확인
        if (principalDetails == null) {
            return mapper.toResponse(post, userLiked);
        }else{
            //정보가 없을시 예외 반환
            Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                    .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
            userLiked = likeRepository.existsByPostAndMember(post, member);
        }
        //이미지 가져오기
        List<PostImage> postImages = post.getImages();;
        post.setImages(postImages);

        return mapper.toResponse(post, userLiked);
    }

    //전체 리스트 가져오기(페이지)
    public Page<PostResponse> getPostList(int page, String keyword, PrincipalDetails principalDetails) {
        Member member;
        if (principalDetails != null) {
            member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                    .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        } else {
            member = null;
        }


        Pageable pageable = PageRequest.of(page - 1, 10); // 페이지 번호와 크기 지정
        Page<Post> posts;

        if (keyword != null && !keyword.isBlank()) {
            // 키워드 검색
            posts = postRepository.findByTitleContainingIgnoreCase(pageable, keyword);
        } else {
            // 키워드가 없으면 전체 게시물 조회
            posts = postRepository.findAll(pageable);
        }

        //정보 없을시 예외 반환
        if (posts.isEmpty()) {
            throw new ApiException(ErrorCode.SPACE_NOT_FOUND);
        }

        return posts.map(post -> {
            String sortedHours = getSortedBusinessHours(post);// 정렬된 영업시간
            boolean isCurrentlyOpen = calculateIsOpen(post);// 현재 영업 여부 확인

            // 회원 정보가 있는 경우 좋아요 여부 확인, 없으면 false
            boolean userLiked = member != null && likeRepository.existsByPostAndMember(post, member);
            //이미지 가져오기
            List<PostImage> postImages = post.getImages();;
            post.setImages(postImages);

            PostResponse response = mapper.toResponse(post, userLiked);

            response.setBusinessHours(sortedHours);
            response.setOpen(isCurrentlyOpen);

            return response;
        });
    }

    //전체 리스트 가져오기(커서기반)
    public List<PostResponse> getPostsByCursor(PostRequest request, PrincipalDetails principalDetails){
        Post lastPost;
        Member member;
        if (principalDetails != null) {
            member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                    .orElseThrow(()->new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        } else {
            member = null;
        }

        int lastLikes = 0;
        double lastDistance = 0.0;
        double[] userLocation = request.getUserLocation(); //유저 위치정보 가져오기
        int lastReviewCount = 0;

        if(request.getPostId()!=null && request.getPostId()>0){
            lastPost = postRepository.findById(request.getPostId()).orElseThrow(()->new ApiException(ErrorCode.SPACE_NOT_FOUND));
            lastLikes = lastPost.getLikes().size(); //마지막 게시물의 좋아요수 가져오기
            lastDistance = calculateDistance(userLocation, lastPost);// 마지막 사용자와의 거리 가져오기
            lastReviewCount = lastPost.getReviews().size();
        }else{
            request.setPostId(0L);
        }

        Boolean isOpen = request.getIsOpen(); //영업정보 가져오기

        List<Post> posts = postRepository.findByCussor(request, lastLikes, lastDistance, lastReviewCount);

        //정보 없을시 예외 반환
        if (posts.isEmpty()) {
            throw new ApiException(ErrorCode.SPACE_NOT_FOUND);
        }

        return posts.stream()
                .map(post -> {
                    String sortedHours = getSortedBusinessHours(post);// 정렬된 영업시간
                    boolean isCurrentlyOpen = calculateIsOpen(post);// 현재 영업 여부 확인
                    double distance = calculateDistance(userLocation, post);
                    boolean userLiked = member != null && likeRepository.existsByPostAndMember(post, member);

                    //이미지 가져오기
                    List<PostImage> postImages = post.getImages();;
                    post.setImages(postImages);

                    PostResponse response = mapper.toResponse(post, userLiked);

                    response.setDistance(distance);
                    response.setBusinessHours(sortedHours);
                    response.setOpen(isCurrentlyOpen);

                    return response;
                })
                .filter(response -> isOpen == null || !isOpen || response.isOpen())// isOpen 인자가 true일 경우, 영업 중인 포스트만 필터링
                .collect(Collectors.toList());
    }

    //게시물 삭제하기
    public PostResponse delete(Long postId, PrincipalDetails principalDetails) {
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId).orElseThrow(() -> new ApiException(ErrorCode.SPACE_NOT_FOUND));

        // 관리자 확인 후 삭제
        if (member.getRole().equals("ROLE_ADMIN")) {
            // 해당 post와 관련된 모든 PostImage와 ReviewImage URL 조회
            List<String> postImageUrls = post.getImages().stream()
                    .map(PostImage::getImageUrl)
                    .toList();

            List<String> reviewImageUrls = post.getReviews().stream()
                    .flatMap(review -> review.getImages().stream())
                    .map(ReviewImage::getImageUrl)
                    .toList();

            //S3에서 이미지 삭제
            List<String> allImageUrls = new ArrayList<>();
            allImageUrls.addAll(postImageUrls);
            allImageUrls.addAll(reviewImageUrls);

            s3Service.deleteImagesFromS3(allImageUrls);

            //Post 삭제 (Cascade로 연관된 엔티티 데이터 삭제)
            postRepository.delete(post);
        }

        return mapper.toResponse(post, false);
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////////Redis 관련 메서드////////////////////////
    //////////////////////////////////////////////////////////////

    public List<PostResponse> getTop10FromRedis(PrincipalDetails principalDetails){
        List<Long> top10Ids = likeRankingService.getTop10PostIds(); // Redis에서 상위 10개 ID

        if (top10Ids.isEmpty()) {
            return Collections.emptyList();
        }

        // DB에서 postId에 해당하는 게시물들 한꺼번에 조회
        List<Post> posts = postRepository.findAllById(top10Ids);

        // List<Post>를 postId 순서대로 재정렬
        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getPostId, p -> p));

        List<Post> sortedPosts = top10Ids.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // userLiked 여부
        Member member;
        if (principalDetails != null) {
            member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                    .orElse(null);
        }else{
            member = null;
        }


        return sortedPosts.stream()
                .map(post -> {
                    boolean userLiked = (member != null) && likeRepository.existsByPostAndMember(post, member);
                    return mapper.toResponse(post, userLiked);
                })
                .collect(Collectors.toList());

    }

    ///////////////////////////////////////////////////////////////
    ///////////서비스에 필요한 계산 등의 메서드////////////////////////
    //////////////////////////////////////////////////////////////

    // 사용자와 게시물 사이의 거리 계산
    private double calculateDistance(double[] userLocation, Post lastPost) {
        // 사용자 위치 가져오기
        double userLat = userLocation[0];
        double userLng = userLocation[1];
        // 마지막 게시물 위치정보 가져오기
        double postLat = lastPost.getLatitude();
        double postLng = lastPost.getLongitude();
        double earthRadiusKm = 6371.0; // 지구 반지름 (단위: km)

        double dLat = Math.toRadians(postLat - userLat);
        double dLng = Math.toRadians(postLng - userLng);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(postLat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c; // 거리 반환 (단위: km)

    }


    // 영업시간 정렬
    private String getSortedBusinessHours(Post post) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<String> koreanDays = Arrays.asList("월", "화", "수", "목", "금", "토", "일");
        //휴일 가져오기
        String[] holidays = post.getHolidays().split(",");

        // 영업시간 파싱
        Map<String, String> businessHoursMap = new LinkedHashMap<>();
        String[] lines = post.getBusinessHours().split("\n");
        boolean isEverydayOnly = false;

        for (String line : lines) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                if (parts[0].equals("매일")) {
                    // "매일"만 있을 경우 모든 요일에 동일한 시간 적용
                    isEverydayOnly = true;
                    for (String day : koreanDays) {
                        businessHoursMap.put(day, parts[1]);
                    }
                } else {
                    businessHoursMap.put(parts[0], parts[1]);
                }
            }
        }
        
        //매일 처리
        if (isEverydayOnly) {
            for (String day : koreanDays) {
                businessHoursMap.putIfAbsent(day, "영업 정보 없음");
            }
        }
        
        //휴일 처리
        for (String holiday : holidays) {
            holiday = holiday.trim();
            if (koreanDays.contains(holiday)) {
                businessHoursMap.put(holiday, "휴무");
            }
        }

        // 오늘 기준으로 요일 정렬
        StringBuilder sortedHours = new StringBuilder();
        int todayIndex = koreanDays.indexOf(today.getDisplayName(TextStyle.SHORT, Locale.KOREAN));
        for (int i = 0; i < 7; i++) {
            int dayIndex = (todayIndex + i) % 7;
            String currentDay = koreanDays.get(dayIndex);
            String hours = businessHoursMap.getOrDefault(currentDay, "영업 정보 없음");
            sortedHours.append(currentDay).append(" : ").append(hours).append("\n");
        }
        return sortedHours.toString().trim();
    }

    // 현재 영업 여부 확인
    private boolean calculateIsOpen(Post post) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        LocalTime nowTime = LocalTime.now();

        // 휴무일 확인
        List<String> holidays = Arrays.asList(post.getHolidays().split(",\\s*"));
        String todayName = today.getDisplayName(TextStyle.SHORT, Locale.KOREAN);
        if (holidays.contains(todayName)) {
            return false; // 휴무일이면 영업 중이 아님
        }

        // 오늘 영업시간 확인
        post.setBusinessHours(post.getBusinessHours().replace("\\n", "\n"));
        String[] lines = post.getBusinessHours().split("\n");
        for (String line : lines) {
            if (line.startsWith(todayName)) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    String[] timeRange = parts[1].split("-");
                    try {
                        LocalTime openTime = LocalTime.parse(timeRange[0].trim());
                        LocalTime closeTime = LocalTime.parse(timeRange[1].trim());
                        Boolean isOpen = !nowTime.isBefore(openTime) && !nowTime.isAfter(closeTime);
                        return isOpen;
                    } catch (Exception e) {
                        return false; // 잘못된 시간 형식
                    }
                }
            }
        }
        return false;
    }

    //주소를 좌표로 변환
    private double[] getGEOCode(String address) throws IOException, ParseException {
        StringBuilder urlBuilder = new StringBuilder("https://dapi.kakao.com/v2/local/search/address.json");
        urlBuilder.append("?" + URLEncoder.encode("query","UTF-8") + "=" + URLEncoder.encode(address,"UTF-8")); //주소

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization","KakaoAK "+KAKAO_CLIENT);
        conn.setRequestProperty("Content-type", "application/json");
        int responseCode = conn.getResponseCode();


        // 응답 실패 시 예외 발생
        if (responseCode < 200 || responseCode >= 300) {
            throw new ApiException(ErrorCode.EXTERNAL_API_ERROR);
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(sb.toString());
        // documents 파싱
        JSONArray documents = (JSONArray) obj.get("documents");
        // documents 로 부터 address찾기
        if (documents.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_ADDRESS);
        }
        JSONObject firstDocument = (JSONObject) documents.get(0);
        double[] GEOCode = new double[2];
        GEOCode[0] = Double.parseDouble((String) firstDocument.get("y")); //위도
        GEOCode[1] = Double.parseDouble((String) firstDocument.get("x")); //경도

        return GEOCode;

    }


}
