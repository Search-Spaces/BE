package nomad.searchspace.domain.post.DTO;

import nomad.searchspace.domain.post.entity.Post;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostMapper {

        // 요청 -> 엔티티
        public Post toEntity(PostRequest request) {
            return Post.builder()
                    .latitude(request.getUserLocation()[0]) //위도
                    .longitude(request.getUserLocation()[1]) //경도
                    .type(request.getPostType())
                    .postId(request.getPostId())
                    .build();
        }

        // DTO -> 엔티티
        public Post toEntity(PostDTO dto) {
            return Post.builder()
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .address(dto.getAddress())
                    .type(dto.getType())
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
                    .phoneNumber(dto.getPhoneNumber())
                    .businessHours(dto.getBusinessHours())
                    .holidays(dto.getHolidays())
                    .url(dto.getUrl())
                    .copyright(dto.isCopyright())
                    .approval(dto.isApproval())
                    .build();
        }

        // 엔티티 -> 응답
        public PostResponse toResponse(Post post) {
            return PostResponse.builder()
                .id(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .address(post.getAddress())
                .type(post.getType())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .phoneNumber(post.getPhoneNumber())
                .businessHours(post.getBusinessHours())
                .holidays(post.getHolidays())
                .url(post.getUrl())
                .copyright(post.isCopyright())
                .approval(post.isApproval())
                .likeCount(post.getLikeCount())
                .build();
        }

    }
