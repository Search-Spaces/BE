package nomad.searchspace.domain.member.service;


import io.jsonwebtoken.JwtException;
import io.lettuce.core.RedisException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.dto.AdditionalMemberInfoRequest;
import nomad.searchspace.domain.member.dto.MemberResponse;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.domain.scrap.repository.ScrapRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import nomad.searchspace.global.util.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ScrapRepository scrapRepository;
    private final RedisService redisService;
    private final JwtUtil jwtUtil;

    @Transactional
    public void updateMember(PrincipalDetails principalDetails, AdditionalMemberInfoRequest memberRequest) {
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND)); // getattributekey로도 검색 해보기

        member.setNickname(memberRequest.getNickname());
        member.setGender(memberRequest.getGender());
        member.setBirth(memberRequest.getBirth());

    }

    public MemberResponse getMemberInfo(PrincipalDetails principalDetails) {
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponse.builder().member(member).build();
    }

    public void logout(PrincipalDetails principalDetails, HttpServletRequest request) {

        String accessToken = request.getHeader("Authorization");
//        String refreshToken = request.getHeader("Refresh"); 굳이 필요 없을듯

        if (accessToken == null) {
            log.warn("Authorization 헤더가 없습니다.");
            throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        if (!accessToken.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 잘못된 형식입니다.");
            throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String originToken = accessToken.substring(7);
        String memberEmail = principalDetails.getMember().getEmail();

        try {
            // Redis에서 Refresh Token 삭제
            String savedRefreshToken = redisService.getRefreshToken(memberEmail);
            if (savedRefreshToken == null || savedRefreshToken.isEmpty()) {
                log.warn("Refresh 토큰이 Redis에 존재하지 않습니다. user: {}", memberEmail);
                throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            redisService.deleteRefreshToken(memberEmail);

            // Access Token 블랙리스트에 저장
            long accessTokenRemainingTime = jwtUtil.getTokenRemainingTime(originToken);
            if (accessTokenRemainingTime <= 0) {
                log.info("Access 토큰이 이미 만료되었습니다. user: {}", memberEmail);
                throw new ApiException(ErrorCode.ACCESS_TOKEN_ALREADY_EXPIRED);
            }
            redisService.saveBlackListToken(originToken, accessTokenRemainingTime);

        } catch (RedisException e) {
            log.error("Redis 처리 중 에러가 발생했습니다. member {}: {}", memberEmail, e.getMessage());
            throw new ApiException(ErrorCode.REDIS_COMMAND_ERROR);
        } catch (JwtException e) {
            log.error("JWT 처리 중 에러가 발생했습니다. member {}: {}", memberEmail, e.getMessage());
            throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
        } catch (Exception e) {
            log.error("로그아웃 진행 중 예상치 못한 에러가 발생했습니다. member {}: {}", memberEmail, e.getMessage());
            throw new RuntimeException("Logout process failed. Please try again.");
        }

    }


}
