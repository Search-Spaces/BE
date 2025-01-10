package nomad.searchspace.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis에 Refresh Token 저장
    public void saveRefreshToken(String memberEmail, String refreshToken) {
        redisTemplate.opsForValue().set("refresh_token:" + memberEmail, refreshToken, 24 * 14, TimeUnit.HOURS); // 2주 후 만료
        log.info("refresh_token:" + memberEmail + refreshToken, 24, TimeUnit.HOURS);
    }

    public void saveBlackListToken(String accessToken, long remainingTime) {
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", remainingTime, TimeUnit.MILLISECONDS); // 2주 후 만료
        log.info("Blacklist token saved: " + accessToken);
    }

    // Redis에서 Refresh Token 조회
    public String getRefreshToken(String memberEmail) {
        return redisTemplate.opsForValue().get("refresh_token:" + memberEmail);
    }

    // Redis에서 Refresh Token 삭제
    public void deleteRefreshToken(String memberEmail) {
        redisTemplate.delete("refresh_token:" + memberEmail);
    }

    // Redis에서 블랙리스트 토큰인지 확인
    public boolean isTokenBlacklisted(String accessToken) {
        return redisTemplate.hasKey("blacklist:" + accessToken);
    }

}
