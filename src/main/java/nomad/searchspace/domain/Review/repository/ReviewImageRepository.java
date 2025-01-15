package nomad.searchspace.domain.Review.repository;

import nomad.searchspace.domain.Review.entity.ReviewImage;
import nomad.searchspace.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
}
