package nomad.searchspace.global.util;

import io.jsonwebtoken.Jwts;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final MemberRepository memberRepository;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret, MemberRepository memberRepository) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.memberRepository = memberRepository;
    }

    public Authentication getAuthentication(String token) {
        Member member = memberRepository.findByEmail(getUserEmail(token))
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOW_FOUND));

        PrincipalDetails principalDetails = new PrincipalDetails(member, null);
        return new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
    }

    // 사용자명 추출
    public String getUserEmail(String token) {
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
    public String createJwt(String category, String userEmail, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("email", userEmail)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}