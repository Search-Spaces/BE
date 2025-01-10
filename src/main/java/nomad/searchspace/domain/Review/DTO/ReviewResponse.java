package nomad.searchspace.domain.Review.DTO;

import lombok.Builder;
import lombok.Data;
import nomad.searchspace.domain.Review.entity.ContentType;
import nomad.searchspace.domain.Review.entity.Review;
import nomad.searchspace.domain.Review.entity.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewResponse {
    private Long reviewId;
    private Long postId;             // 대상 Post ID
    private List<ContentType> contentTypes;
    private String authorEmail;
    private String content;
    private LocalDateTime createDate;
    private List<ReviewImageResponse> images;
}

