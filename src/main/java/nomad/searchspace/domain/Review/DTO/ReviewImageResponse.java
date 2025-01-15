package nomad.searchspace.domain.Review.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewImageResponse {
    private String url;
    private String description;
}
