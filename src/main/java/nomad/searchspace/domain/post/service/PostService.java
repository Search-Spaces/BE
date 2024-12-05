package nomad.searchspace.domain.post.service;

import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.like.repository.LikeRepository;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.domain.post.DTO.PostDTO;
import nomad.searchspace.domain.post.DTO.PostMapper;
import nomad.searchspace.domain.post.DTO.PostRequest;
import nomad.searchspace.domain.post.DTO.PostResponse;
import nomad.searchspace.domain.post.entity.Post;
import nomad.searchspace.domain.post.repository.PostRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper mapper;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;

    @Value("${KAKAO_SECRET}")
    private String KAKAO_SECRET;


    //게시물 생성 요청 + 이미지 부분 추가 필요
    public PostResponse create(PostDTO dto) throws IOException, ParseException {
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
        post = postRepository.save(post);
        return mapper.toResponse(post, false);
    }

    //특정 id로 상세정보 가져오기 + 이미지 부분 추가 필요
    public PostResponse getPost(Long postId, PrincipalDetails principalDetails) {
        Post post = postRepository.findById(postId).orElseThrow(()->new ApiException(ErrorCode.SPACE_NOT_FOUND));
        boolean userLiked=false;
        // PrincipalDetails가 null인지 확인
        if (principalDetails == null) {
            return mapper.toResponse(post, userLiked);
        }else{
            //정보가 없을시 예외 반환
            Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                    .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOW_FOUND));
            userLiked = likeRepository.existsByPostAndMember(post, member);
        }

        return mapper.toResponse(post, userLiked);
    }

    //전체 리스트 가져오기(페이지) + 이미지 관련 추가 필요
    public Page<PostResponse> getPostList(int page, String keyword, PrincipalDetails principalDetails) {
        Member member;
        if (principalDetails != null) {
            member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                    .orElse(null);
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
            // 회원 정보가 있는 경우 좋아요 여부 확인, 없으면 false
            boolean userLiked = member != null && likeRepository.existsByPostAndMember(post, member);
            return mapper.toResponse(post, userLiked);
        });
    }

    //전체 리스트 가져오기(커서기반) + 이미지 및 리뷰순 관련 수정 필요
    public List<PostResponse> getPostsByCursor(PostRequest request, PrincipalDetails principalDetails){
        Post lastPost;
        Member member;
        if (principalDetails != null) {
            member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                    .orElse(null);
        } else {
            member = null;
        }

        int lastLikes = 0;
        double lastDistance = 0.0;
        double[] userLocation = request.getUserLocation(); //유저 위치정보 가져오기
        
        if(request.getPostId()!=null && request.getPostId()>0){
            lastPost = postRepository.findById(request.getPostId()).orElseThrow(()->new ApiException(ErrorCode.SPACE_NOT_FOUND));
            lastLikes = lastPost.getLikes().size(); //마지막 게시물의 좋아요수 가져오기
            lastDistance = calculateDistance(userLocation, lastPost);// 마지막 사용자와의 거리 가져오기
        }else{
            request.setPostId(0L);
        }

        Boolean isOpen = request.getIsOpen(); //영업정보 가져오기

        List<Post> posts = postRepository.findByCussor(request, lastLikes, lastDistance);

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

                    PostResponse response = mapper.toResponse(post, userLiked);

                    response.setDistance(distance);
                    response.setBusinessHours(sortedHours);
                    response.setOpen(isCurrentlyOpen);

                    return response;
                })
                .filter(response -> isOpen == null || !isOpen || response.isOpen())// isOpen 인자가 true일 경우, 영업 중인 포스트만 필터링
                .collect(Collectors.toList());
    }








    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //여기부터는 서비스에 필요한 계산 등의 메서드


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

        // 영업시간 파싱
        Map<String, String> businessHoursMap = new LinkedHashMap<>();
        String[] lines = post.getBusinessHours().split("\n");
        for (String line : lines) {
            String[] parts = line.split(" ", 2);
            if (parts.length == 2) {
                businessHoursMap.put(parts[0], parts[1]);
            }
        }

        // 오늘 기준으로 요일 정렬
        StringBuilder sortedHours = new StringBuilder();
        int todayIndex = koreanDays.indexOf(today.getDisplayName(TextStyle.SHORT, Locale.KOREAN));
        for (int i = 0; i < 7; i++) {
            int dayIndex = (todayIndex + i) % 7;
            String currentDay = koreanDays.get(dayIndex);
            String hours = businessHoursMap.getOrDefault(currentDay, "영업 정보 없음");
            sortedHours.append(currentDay).append(": ").append(hours).append("\n");
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
        conn.setRequestProperty("Authorization","KakaoAK "+KAKAO_SECRET); // 서비스키 나중에 환경변수 처리
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
