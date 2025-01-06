package nomad.searchspace.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret, MemberRepository memberRepository, RedisTemplate<String, String> redisTemplate) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    public Authentication getAuthentication(String token) {
        Member member = memberRepository.findByEmail(getMemberEmail(token))
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        PrincipalDetails principalDetails = new PrincipalDetails(member, null);
        return new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
    }

    // 사용자명 추출
    public String getMemberEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
    }

    // 권한 추출
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // token 유효확인
    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    // accessToken인지 refreshToken 인지 확인
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    // JWT 발급
    public String createAccessToken(String memberEmail, String role) {
        return Jwts.builder()
                .claim("email", memberEmail)
                .claim("category","access_token")
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 30))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(String memberEmail) {
        String refreshToken = Jwts.builder()
                .claim("email",memberEmail)
                .claim("category","refresh_token")
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 14)) // 2주
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // Redis에 저장
        redisTemplate.opsForValue().set("refresh_token:" + memberEmail, refreshToken, 1000L * 60 * 60 * 24 * 14, TimeUnit.MILLISECONDS); // 2 주 후 만료
        return refreshToken;
    }
}