package nomad.searchspace.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    @Builder
    public PostImage(String imageUrl, String description, Post post) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.post = post;
    }
}
