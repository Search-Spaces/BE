package nomad.searchspace.domain.post.DTO;

import lombok.Builder;
import lombok.Data;
import nomad.searchspace.domain.post.entity.PostType;

@Data
@Builder
public class PostRequest {
    private double[] userLocation;
    private Double topLeftLat;
    private Double topLeftLng;
    private Double bottomRightLat;
    private Double bottomRightLng;
    private Long postId;
    private int limit = 10;
    private String keyword;
    private PostType postType;
    private Boolean isOpen;
    private OrderBy orderBy;

}