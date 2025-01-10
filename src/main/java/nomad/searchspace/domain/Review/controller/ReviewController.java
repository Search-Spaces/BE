package nomad.searchspace.domain.Review.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.Review.DTO.PostReviewTotalResponse;
import nomad.searchspace.domain.Review.DTO.ReviewRequest;
import nomad.searchspace.domain.Review.DTO.ReviewResponse;
import nomad.searchspace.domain.Review.sevice.ReviewService;
import nomad.searchspace.global.auth.PrincipalDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "특정 게시물의 전체 리뷰 가져오기(커서)", description = "특정 게시물 id 값과 마지막 리뷰의 id값 으로 리뷰들을 가지고 오는 api 입니다.")
    @GetMapping("get/{postId}")
    public ResponseEntity<PostReviewTotalResponse> getReview(@PathVariable Long postId,  @RequestParam(required = false) Long reviewId,
                                                             @RequestParam(defaultValue = "10") int limit) {
        PostReviewTotalResponse response = reviewService.getReview(postId, reviewId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 등록", description = "리뷰를 등록하는 api 입니다.")
    @PostMapping("create")
    public ResponseEntity<ReviewResponse> createReview(@RequestPart ReviewRequest dto, @RequestPart List<MultipartFile> images,
                                                       @AuthenticationPrincipal PrincipalDetails principalDetails) {
        ReviewResponse response = reviewService.createReview(dto, images, principalDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 삭제", description = "관리자가 부적절한 리뷰를 삭제하는 api 입니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<ReviewResponse> deleteReview(@RequestParam Long reviewId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        ReviewResponse response = reviewService.deleteReview(reviewId, principalDetails);
        return ResponseEntity.ok(response);
    }
}
