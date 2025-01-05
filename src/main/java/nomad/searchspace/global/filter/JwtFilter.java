package nomad.searchspace.global.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import nomad.searchspace.global.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 요청 헤더에 있는 Authorization라는 값을 가져오자 이게 accessToken이다.
        String accessToken = request.getHeader("Authorization");

        // 요청 헤더에 Authorization이 없는 경우
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 제거
        String originToken = accessToken.substring(7);

        // 토큰이 유효한지 확인 후 클라리언트로 상태 코드 응답
        try {
            if (jwtUtil.isExpired(originToken)) {
                PrintWriter writer = response.getWriter();
                writer.println("access token expired");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (ExpiredJwtException e) {
            PrintWriter writer = response.getWriter();
            writer.println("access token expired");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // accessToken인지 refreshToken인지 확인
        String category = jwtUtil.getCategory(originToken);

        // JwtFilter는 요청에 대해 accessToken만 취급하므로 access인지 확인
        if (!"access_token".equals(category)) {
            PrintWriter writer = response.getWriter();
            writer.println("invalid access token");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 사용자명과 권한을 accessToken에서 추출
        String userEmail = jwtUtil.getMemberEmail(originToken);

//        Member member = memberRepository.findByEmail(userEmail).orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOW_FOUND));
//        PrincipalDetails principalDetails = new PrincipalDetails(member, null, member.getKakaoId());

        Authentication authentication = jwtUtil.getAuthentication(originToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // 로그인 흐름 다시 보기

        filterChain.doFilter(request, response);
    }
}
