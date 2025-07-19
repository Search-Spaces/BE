package nomad.searchspace.domain.member.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nomad.searchspace.domain.member.service.RedisService;
import nomad.searchspace.global.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class JWTController {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    @PostMapping("/api/member/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        //get refresh token
        String refresh = null;
        String access = request.getHeader("Authorization"); // 바꿔야하나

        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            //response status code
            return ResponseEntity.badRequest().body("Refresh Token not exist");
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            //response status code
            return ResponseEntity.badRequest().body("토큰이 만료 되었습니다.");
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh_token")) {
            //response status code
            return ResponseEntity.badRequest().body("Invalid Refresh Token");
        }

        //Redis에 저장되어 있는지 확인
        String memberEmail = jwtUtil.getMemberEmail(access);
        String role = jwtUtil.getRole(access);

        log.info("####### username ####### {}",memberEmail);
        log.info("####### role ####### {}",role);

        String storedRefreshToekn = redisService.getRefreshToken(memberEmail);

        if (storedRefreshToekn == null || !storedRefreshToekn.equals(refresh)) {
            //response body
            return ResponseEntity.badRequest().body("Invalid Refresh Token or Refresh Token not exist");
        }

        //make new JWT
        String newAccess = jwtUtil.createAccessToken(memberEmail, role);
        String newRefresh = jwtUtil.createRefreshToken(memberEmail);

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        redisService.saveRefreshToken(memberEmail,newRefresh); // todo 덮어씌울지 지우고 다시 저장할지

        //response
        response.setHeader("access", newAccess); // todo 계속 쿠키에 담을까?
        response.addCookie(createCookie("refresh", newRefresh, 1209600));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 쿠키 생성 메서드
    private Cookie createCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }

}
