package nomad.searchspace.domain.member.service;


import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.dto.AdditionalMemberInfoRequest;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void updateMember(PrincipalDetails principalDetails, AdditionalMemberInfoRequest memberRequest) {
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND)); // getattributekey로도 검색 해보기

        member.setNickname(memberRequest.getNickname());
        member.setGender(memberRequest.getGender());
        member.setBirth(memberRequest.getBirth());
        member.setPhoneNumber(memberRequest.getPhoneNumber());

    }

}
