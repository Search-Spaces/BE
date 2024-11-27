package nomad.searchspace.domain.post.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostImageDTO {
    private Long id;
    private String url;
    private String description;
}
