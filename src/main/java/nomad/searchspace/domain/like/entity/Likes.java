package nomad.searchspace.domain.like.entity;


import jakarta.persistence.*;
import lombok.*;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.post.entity.Post;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private Member member;

    @Builder
    public Likes(Post post, Member member) {
        this.post = post;
        this.member = member;
    }
}
