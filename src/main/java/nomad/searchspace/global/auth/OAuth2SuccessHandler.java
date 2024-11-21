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
        String accessToken = jwtUtil.createJwt("access", userEmail, role, 3600000L);
        // refreshToken 만료시간 : 2주
        String refreshToken = jwtUtil.createJwt("refresh", userEmail, role, 1209600000L);

        // redis에 insert (key = username, value = refreshToken)

        // refreshToken은 쿠키를 통하여 응답
        response.addCookie(createCookie("refresh_token", refreshToken, 1209600));
        response.setStatus(HttpServletResponse.SC_OK);

        if (isExistingUser) {
            // 프론트엔드에서 리다이렉트를 받으면 헤더값은 바로 빼낼 수 없기 때문에, URL 파라미터로 access token을 전달
            // 기존 회원
            response.sendRedirect("http://localhost:3000/landing/authcallback/?access_token=" + accessToken);
        }
        else {
            // 신규 회원
            response.sendRedirect("http://localhost:80/new-user?access_token=" + accessToken);
        }

    }

    // 쿠키 생성 메서드
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
//        cookie.setSecure();
        cookie.setHttpOnly(true);
        return cookie;
    }
}
