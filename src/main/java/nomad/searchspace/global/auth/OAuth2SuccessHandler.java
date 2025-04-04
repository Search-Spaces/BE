package nomad.searchspace.global.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.global.util.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        Boolean isExistingUser = (Boolean) principalDetails.getAttributes().get("exist");
        // 토큰 생성시에 사용자명과 권한이 필요
        String userEmail = principalDetails.getUsername();//Todo 확인해야함.

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // accessToken과 refreshToken 생성
        // accessToken 만료시간 : 1시간
        String accessToken = jwtUtil.createAccessToken(userEmail, role);
        // refreshToken 만료시간 : 2주
        String refreshToken = jwtUtil.createRefreshToken(userEmail);

        // 토큰을 쿠키를 통하여 응답
        response.addCookie(createCookie("access", accessToken, 3600));
        response.addCookie(createCookie("refresh", refreshToken, 1209600));
        response.setStatus(HttpServletResponse.SC_OK);

        if (isExistingUser) {
            // 기존 회원
            response.sendRedirect("https://searchspace.store/map");
        }
        else {
            // 신규 회원
            response.sendRedirect("https://searchspace.store/signup"); // todo 새로운 정보 안 썼을 경우 다시 이 쪽으로 리디렉션 시키기?
        }

    }

    // 쿠키 생성 메서드
    private Cookie createCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}
