package nomad.searchspace.domain.Review.DTO;

import lombok.Builder;
import lombok.Data;
import nomad.searchspace.domain.Review.entity.ContentType;

import java.util.List;

@Data
@Builder
public class ReviewRequest {
    private Long postId;
    private List<ContentType> contentTypesList;
    private String content;
}
