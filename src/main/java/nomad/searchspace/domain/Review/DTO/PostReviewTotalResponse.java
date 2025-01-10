package nomad.searchspace.domain.Review.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostReviewTotalResponse {
    private Long postId;

    private Long cleanCount;
    private Long comfortableCount;
    private Long parkCount;
    private Long deliciousCount;
    private Long concentrateCount;

    // 여러 리뷰를 담은 리스트
    private List<ReviewContentResponse> reviews;
}
