package nomad.searchspace.domain.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeRankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String ZSET_KEY = "post:likeRanking"; // Sorted Set 키

    //like 점수 증감
    public void incrementLikeCount(Long postId, int incr) {
        // postId를 String으로 변환해 key로 사용
        String postIdStr = String.valueOf(postId);
        // ZINCRBY
        redisTemplate.opsForZSet().incrementScore(ZSET_KEY, postIdStr, incr);
    }

    //좋아요 상위 10개 게시물 id조회
    public List<Long> getTop10PostIds() {
        // 0~9 범위를 score 내림차순(Reverse Range)로 조회
        // ZREVRANGE -> 반환 타입: Set<String>
        Set<String> topIds = redisTemplate.opsForZSet()
                .reverseRange(ZSET_KEY, 0, 9);

        if (topIds == null || topIds.isEmpty()) {
            return Collections.emptyList();
        }

        // String → Long 변환
        return topIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    //점수 가져오기
    public LinkedHashMap<Long, Double> getTop10WithScores() {
        // reverseRangeWithScores로 (게시물, 점수) 쌍을 얻는다.
        Set<ZSetOperations.TypedTuple<String>> rangeWithScores =
                redisTemplate.opsForZSet().reverseRangeWithScores(ZSET_KEY, 0, 9);

        if (rangeWithScores == null || rangeWithScores.isEmpty()) {
            return new LinkedHashMap<>();
        }

        LinkedHashMap<Long, Double> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<String> tuple : rangeWithScores) {
            String postIdStr = tuple.getValue();
            Double score = tuple.getScore();
            if (postIdStr != null && score != null) {
                result.put(Long.valueOf(postIdStr), score);
            }
        }
        return result;
    }
}