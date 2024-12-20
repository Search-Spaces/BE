package nomad.searchspace.domain.post.DTO;

import nomad.searchspace.domain.post.entity.Post;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class PostMapper {

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
        public PostResponse toResponse(Post post, boolean userLiked) {
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
                    .likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
                    .userLiked(userLiked)
                    .images(post.getImages().stream() // PostImage 엔티티를 DTO로 변환
                            .map(image -> PostImageResponse.builder()
                                    .url(image.getImageUrl())
                                    .description(image.getDescription())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }

    }
