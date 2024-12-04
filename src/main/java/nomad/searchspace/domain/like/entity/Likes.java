package nomad.searchspace.domain.like.entity;


import jakarta.persistence.*;
import lombok.*;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.post.entity.Post;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false, updatable = false, insertable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false, updatable = false, insertable = false)
    private Member member;
}
