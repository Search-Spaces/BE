package nomad.searchspace.domain.Review.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewContentResponse {
    private String content;      // 리뷰 텍스트
    private String authorEmail;  // 작성자 이메일
    private String timeAgo;
    private List<ReviewImageResponse> images;
}
