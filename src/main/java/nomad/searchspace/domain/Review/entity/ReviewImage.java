package nomad.searchspace.domain.Review.entity;

import jakarta.persistence.*;
import lombok.*;
import nomad.searchspace.domain.post.entity.Post;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewImageId;

    @Column(length = 250)
    private String imageUrl;
    @Column(length = 250) //이미지 설명
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewId", nullable = false)
    private Review review;

    @Builder
    public ReviewImage(String imageUrl, String description, Review review) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.review = review;
    }
}
