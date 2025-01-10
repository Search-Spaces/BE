package nomad.searchspace.domain.post.DTO;

import lombok.Builder;
import lombok.Data;
import nomad.searchspace.domain.post.entity.PostType;

import java.util.List;

@Data
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String address;
    private PostType type;
    private double latitude;
    private double longitude;
    private String phoneNumber;
    private String businessHours;
    private String holidays;
    private String url;
    private boolean copyright;
    private boolean approval;
    private int likeCount;
    private int reviewCount;
    private boolean userLiked;
    private String createEmail;

    private boolean isOpen;
    private double distance;


    private List<PostImageResponse> images;
}
