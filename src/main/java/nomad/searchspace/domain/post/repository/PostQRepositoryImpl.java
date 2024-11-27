package nomad.searchspace.domain.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.post.DTO.OrderBy;
import nomad.searchspace.domain.post.DTO.PostRequest;
import nomad.searchspace.domain.post.entity.Post;
import nomad.searchspace.domain.post.entity.QPost;
import nomad.searchspace.domain.post.entity.QPostImage;


import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
public class PostQRepositoryImpl implements PostQRepository {
    private final JPAQueryFactory queryFactory;
    QPost post = QPost.post;
    QPostImage postImage = QPostImage.postImage;

    @Override
    public List<Post> findByCussor(PostRequest request, int lastLikes, double lastDistance) {
        OrderSpecifier<?> orderBy = getOrderBy(request);
        BooleanBuilder whereClause = getWhereClause(request, lastLikes, lastDistance);
        return queryFactory
                .select(post)
                .from(post)
                .leftJoin(post.images, postImage).fetchJoin()
                .where(whereClause.and(post.approval.eq(true))) // 승인된 게시물만
                .orderBy(orderBy, post.postId.asc())
                .limit(request.getLimit())
                .fetch();
    }


    //정렬기준 가져오기
    private OrderSpecifier<?> getOrderBy(PostRequest request) {
        // 정렬 방식 결정
        OrderSpecifier<?> orderBy = null;
        if (request.getOrderBy() == OrderBy.DISTANCE && request.getUserLocation() != null) {
            // 거리순 정렬 (Haversine 공식 사용)
            double earthRadiusKm = 6371.0; // 지구 반지름 (km)
            double latitude = request.getUserLocation()[0]; // 사용자 위도
            double longitude = request.getUserLocation()[1]; // 사용자 경도

            orderBy = Expressions.numberTemplate(Double.class,
                    "{0} * acos(cos(radians({1})) * cos(radians({2})) * cos(radians({3}) - radians({4})) + sin(radians({1})) * sin(radians({2})))",
                    earthRadiusKm, latitude, post.latitude, longitude, post.longitude // 경도 값 반영
            ).asc(); // 거리 순 오름차순
        } else if (request.getOrderBy() == OrderBy.RECOMMENDED) {
            // 추천순 정렬 (likeCount 기준)
            orderBy = post.likeCount.desc();
        } else if (request.getOrderBy() == OrderBy.REVIEW) {
            //리뷰순 정렬 (리뷰 완료후 추가 예정)
        }
        return orderBy;
    }
    
    //검색조건 가져오기
    private BooleanBuilder getWhereClause(PostRequest request, int lastLikes, double lastDistance) {
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(post.postId.ne(request.getPostId())); // 동일 ID 제외
        // 키워드 검색 조건
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            whereClause.and(post.title.containsIgnoreCase(request.getKeyword()));
        }

        // 타입 필터
        if (request.getPostType() != null) {
            whereClause.and(post.type.eq(request.getPostType()));
        }

        // 지도 범위 필터
        if (request.getTopLeftLat() != null && request.getTopLeftLng() != null
                && request.getBottomRightLat() != null && request.getBottomRightLng() != null) {
            whereClause.and(post.latitude.between(request.getBottomRightLat(), request.getTopLeftLat()));
            whereClause.and(post.longitude.between(request.getTopLeftLng(), request.getBottomRightLng()));
        }
        //추천순 커서 조건
        if (request.getOrderBy() == OrderBy.RECOMMENDED){
            whereClause.and(
                    post.likeCount.lt(lastLikes).or(post.likeCount.eq(lastLikes).and(post.postId.gt(request.getPostId())))
            );
        // 거리순 커서 조건
        }else if (request.getOrderBy() == OrderBy.DISTANCE && request.getUserLocation() != null) {
            double userLat = request.getUserLocation()[0];
            double userLng = request.getUserLocation()[1];
            double earthRadiusKm = 6371.0;

            // 거리 계산
            NumberExpression<Double> distanceExpression = Expressions.numberTemplate(Double.class,
                    "{0} * acos(cos(radians({1})) * cos(radians({2})) * cos(radians({3}) - radians({4})) + sin(radians({1})) * sin(radians({2})))",
                    earthRadiusKm, userLat, post.latitude, userLng, post.longitude);

            whereClause.and(distanceExpression.gt(lastDistance)
                    .or(distanceExpression.eq(lastDistance).and(post.postId.gt(request.getPostId())))
            );
        //리뷰순 커서 조건(리뷰 완료후 추가 예정)
        }else if (request.getOrderBy() == OrderBy.REVIEW){

        }
        return whereClause;
    }



}
