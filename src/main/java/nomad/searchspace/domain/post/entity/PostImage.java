package nomad.searchspace.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postImageId;

    @Column(length = 250)
    private String imageUrl;
    @Column(length = 250) //이미지 설명
    private String description;

    @ManyToOne
    @JoinColumn(name = "postId", nullable = false, updatable = false, insertable = false)
    private Post post;
}
