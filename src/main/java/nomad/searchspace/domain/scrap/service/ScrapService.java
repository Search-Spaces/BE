package nomad.searchspace.domain.scrap.service;

import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.domain.post.entity.Post;
import nomad.searchspace.domain.post.repository.PostRepository;
import nomad.searchspace.domain.scrap.domain.Scrap;
import nomad.searchspace.domain.scrap.repository.ScrapRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public void editScrap(Long postId, PrincipalDetails principalDetails) {
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<Scrap> scrap = scrapRepository.findByPost_PostIdAndMemberId(postId, member.getId());

        if (scrap.isPresent()) {
//            scrapRepository.deleteById(scrap.get().getScraoId());
            scrapRepository.delete(scrap.get());
            // TODO 공부 : 위 메서드는 즉시 scrap 엔티티에 대해 영속성 컨텍스트에서 삭제 표시를 하기때문에
            //  동일 트랜잭션에서 다시 엔티티를 사용할 때 오류가 나지 않지만 byid를 사용하면
            //  영속성 컨텍스트에 표시가 안되고 트랜잭션이 끝난 후 삭제 처리가 돼서 오류가 발생할 수 있음.
            //  다른 트랜잭션에서 삭제 도중 접근하려하면 기본적으로 READ COMMITTED 이기 때문에 lock이 걸리므로 대기나 에러 발생.
        }
        else {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new ApiException(ErrorCode.SPACE_NOT_FOUND));
            Scrap newScrap = Scrap.builder()
                    .post(post)
                    .member(member)
                    .build();

            member.getScraps().add(newScrap);
        }
    }

}
