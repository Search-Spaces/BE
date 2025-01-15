package nomad.searchspace.domain.Review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.Review.entity.QReview;
import nomad.searchspace.domain.Review.entity.QReviewImage;
import nomad.searchspace.domain.Review.entity.Review;

import java.util.List;

@RequiredArgsConstructor
public class ReviewQRepositoryImpl implements ReviewQRepository {
    private final JPAQueryFactory queryFactory;
    QReview qReview = QReview.review;
    QReviewImage qReviewImage = QReviewImage.reviewImage;

    @Override
    public List<Review> findReviewsByCussor(Long postId, Long reviewId, int limit) {
        BooleanBuilder whereClause = new BooleanBuilder();
        if(reviewId != null) {
            whereClause.and(qReview.post.postId.eq(postId).and(qReview.reviewId.lt(reviewId)).and(qReview.reviewId.ne(reviewId)));
        }
        return queryFactory
                .select(qReview)
                .from(qReview)
                .leftJoin(qReview.images, qReviewImage).fetchJoin()
                .where(whereClause)
                .orderBy(qReview.createdDate.desc())
                .limit(limit)
                .fetch();
    }

}
