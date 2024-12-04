package nomad.searchspace.domain.like.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeResponse {
    private Long memberId;
    private Long postId;
    private boolean liked;
    private int likeCount;

}
