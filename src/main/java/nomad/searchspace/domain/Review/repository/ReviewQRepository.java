package nomad.searchspace.domain.Review.repository;

import nomad.searchspace.domain.Review.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewQRepository {
    List<Review> findReviewsByCussor(Long postId, Long reviewId, int limit);
}
