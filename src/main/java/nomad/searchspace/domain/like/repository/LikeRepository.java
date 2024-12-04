package nomad.searchspace.domain.like.repository;


import nomad.searchspace.domain.like.entity.Likes;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Likes, Long> {
    int countByPost(Post post); // 특정 게시물의 좋아요 수 조회
    boolean existsByPostAndMember(Post post, Member member); // 특정 유저가 특정 게시물에 좋아요했는지 확인
    void deleteByMemberAndPost(Member member, Post post);
}
