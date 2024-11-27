package nomad.searchspace.domain.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.global.auth.KakaoResponse;
import nomad.searchspace.global.auth.OAuth2Response;
import nomad.searchspace.global.auth.PrincipalDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info(userRequest.toString());

        // 부모 클래스의 메서드를 사용하여 객체를 생성함.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        // 제공자
        String registration = userRequest.getClientRegistration().getRegistrationId();

//        String accessToken = userRequest.getAccessToken().getTokenValue();

        OAuth2Response oAuth2Response = new KakaoResponse(attributes);

        String userEmail = oAuth2Response.getEmail();

        // 넘어온 회원정보가 이미 DB에 존재하는지 확인
        Optional<Member> findMember = memberRepository.findByEmail(userEmail); // 값이 뭔지 궁금. 고유값인지.

        if (findMember.isEmpty()) {
            oAuth2Response.getAttributes().put("exist", false);

            Member member = Member.builder()
                    .email(userEmail)
                    .role("ROLE_USER")
                    .build();
            log.info("회원 가입 하는 곳.");
            memberRepository.save(member);


            return new PrincipalDetails(member, oAuth2Response); // 이거 처리
        }
        else {
            oAuth2Response.getAttributes().put("exist", true);
            log.info("회원 가입 안 하는 곳.");
            return new PrincipalDetails(findMember.get(), oAuth2Response);
        }

    }
}

