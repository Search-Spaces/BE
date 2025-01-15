package nomad.searchspace.domain.Review.sevice;

import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.Review.DTO.PostReviewTotalResponse;
import nomad.searchspace.domain.Review.DTO.ReviewMapper;
import nomad.searchspace.domain.Review.DTO.ReviewRequest;
import nomad.searchspace.domain.Review.DTO.ReviewResponse;
import nomad.searchspace.domain.Review.entity.Review;
import nomad.searchspace.domain.Review.entity.ReviewImage;
import nomad.searchspace.domain.Review.repository.ReviewImageRepository;
import nomad.searchspace.domain.Review.repository.ReviewRepository;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.domain.post.entity.Post;
import nomad.searchspace.domain.post.repository.PostRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import nomad.searchspace.global.service.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final ReviewMapper mapper;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewRepository reviewRepository;

    //리뷰 가져오기
    public PostReviewTotalResponse getReview(Long postId, Long reviewId, int limit) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.SPACE_NOT_FOUND));

        List<Review> reviews = reviewRepository.findReviewsByCussor(post.getPostId(), reviewId, limit);
        if(reviews.isEmpty()){
            throw new ApiException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return mapper.toPostReviewTotalResponse(postId, reviews);
    }

    //리뷰 등록
    public ReviewResponse createReview(ReviewRequest dto, List<MultipartFile> images, PrincipalDetails principalDetails) {

        //회원정보가 없을시 예외 반환
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        //post 정보 없을시 에러 반환
        Post post = postRepository.findById(dto.getPostId()).orElseThrow(
                ()-> new ApiException(ErrorCode.SPACE_NOT_FOUND));

        Review review = mapper.toEntity(dto, post, member);

        //이미지 업로드
        List<ReviewImage> reviewImages = new ArrayList<>();
        int count = 1;
        for (MultipartFile image : images) {
            String imageUrl = s3Service.upload(image);

            ReviewImage reviewImage = ReviewImage.builder()
                    .imageUrl(imageUrl)
                    .description("postId"+review.getPost().getPostId()+"의리뷰사진"+(count++))
                    .review(review)
                    .build();
            reviewImages.add(reviewImage);
        }
        reviewImageRepository.saveAll(reviewImages);

        review.setImages(reviewImages);

        return mapper.toResponse(review);
    }

    //리뷰 삭제
    public ReviewResponse deleteReview(Long reviewId, PrincipalDetails principalDetails) {
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId).orElseThrow(()->new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        // 관리자 확인 후 삭제
        if (member.getRole().equals("ROLE_ADMIN")) {
            //ReviewImage URL 조회
            List<String> reviewImageUrls = review.getImages().stream()
                    .map(ReviewImage::getImageUrl).toList();
            s3Service.deleteImagesFromS3(reviewImageUrls);
            reviewRepository.delete(review);
        }
        return mapper.toResponse(review);
    }

}
