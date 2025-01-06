package nomad.searchspace.domain.scrap.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.post.entity.Post;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Scrap {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ScrapId;

    @ManyToOne
    private Post post;

    @ManyToOne
    private Member member;

    @Builder
    private Scrap(Post post, Member member) {
        this.post = post;
        this.member = member;
    }

}
