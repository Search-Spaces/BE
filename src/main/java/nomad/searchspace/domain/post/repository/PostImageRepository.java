package nomad.searchspace.domain.post.repository;

import nomad.searchspace.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findByPostPostId(Long postId);
}
