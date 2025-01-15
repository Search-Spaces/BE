package nomad.searchspace.domain.Review.DTO;

import nomad.searchspace.domain.Review.entity.ContentType;
import nomad.searchspace.domain.Review.entity.Review;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.post.entity.Post;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {
    //DTO -> 엔티티
    public Review toEntity(ReviewRequest dto, Post post, Member member) {
        return Review.builder()
                .member(member)
                .post(post)
                .content(dto.getContent())
                .contentTypeList(dto.getContentTypesList())
                .build();
    }
    //단일 리뷰 응답
    public ReviewResponse toResponse(Review review){
        String authorEmail = (review.getMember() != null)
                ? review.getMember().getEmail()
                : null;

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .postId(review.getPost().getPostId())
                .content(review.getContent())
                .contentTypes(review.getContentTypesList())
                .authorEmail(authorEmail)
                .createDate(review.getCreatedDate())
                .images(review.getImages().stream()
                        .map(image -> ReviewImageResponse.builder()
                                .url(image.getImageUrl())
                                .description(image.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    //post 의 전체 리뷰 응답
    public PostReviewTotalResponse toPostReviewTotalResponse(Long postId, List<Review> reviews) {
        //카운트 계산
        long cleanCount = reviews.stream()
                .filter(review -> review.getContentTypesList().contains(ContentType.CLEAN))
                .count();

        long comfortableCount = reviews.stream()
                .filter(review -> review.getContentTypesList().contains(ContentType.COMFORTABLE))
                .count();

        long parkCount = reviews.stream()
                .filter(review -> review.getContentTypesList().contains(ContentType.PARK))
                .count();

        long deliciousCount = reviews.stream()
                .filter(review -> review.getContentTypesList().contains(ContentType.DELICIOUS))
                .count();

        long concentrateCount = reviews.stream()
                .filter(review -> review.getContentTypesList().contains(ContentType.CONCENTRATE))
                .count();

        //리뷰들을 ReviewContentResponse로 변환
        List<ReviewContentResponse> reviewContents = reviews.stream()
                .map(this::toReviewContentResponse)  // 아래 private 메서드 활용
                .collect(Collectors.toList());

        //최종 Aggregation DTO 생성
        return PostReviewTotalResponse.builder()
                .postId(postId)
                .cleanCount(cleanCount)
                .concentrateCount(concentrateCount)
                .parkCount(parkCount)
                .deliciousCount(deliciousCount)
                .comfortableCount(comfortableCount)
                .reviews(reviewContents)
                .build();
    }

    // 리뷰 엔티티 -> ReviewContentResponse (작성자 이메일 + 리뷰 내용)
    private ReviewContentResponse toReviewContentResponse(Review review) {
        String authorEmail = (review.getMember() != null) ? review.getMember().getEmail() : null;

        return ReviewContentResponse.builder()
                .content(review.getContent())
                .authorEmail(authorEmail)
                .timeAgo(timeAgo(review.getCreatedDate()))
                .images(review.getImages().stream()
                        .map(image -> ReviewImageResponse.builder()
                                .url(image.getImageUrl())
                                .description(image.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
    
    
    //시간, 날짜 계산
    public static String timeAgo(LocalDateTime createdDate) {
        LocalDateTime now = LocalDateTime.now();

        // createdDate가 미래인 경우 방어 로직
        if (createdDate.isAfter(now)) {
            return "방금 전";
        }

        // 분 단위로 먼저 구하기
        long minutes = Duration.between(createdDate, now).toMinutes();

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = hours / 24;
        if (days < 30) {
            return days + "일 전";
        }

        long months = days / 30;  // 1개월 = 30일로 단순 가정
        if (months < 12) {
            return months + "달 전";
        }

        long years = months / 12; // 1년 = 12개월로 단순 가정
        return years + "년 전";
    }
}

