package nomad.searchspace.domain.like.service;

import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.like.DTO.LikeResponse;
import nomad.searchspace.domain.like.entity.Likes;
import nomad.searchspace.domain.like.repository.LikeRepository;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.member.repository.MemberRepository;
import nomad.searchspace.domain.post.entity.Post;
import nomad.searchspace.domain.post.repository.PostRepository;
import nomad.searchspace.global.auth.PrincipalDetails;
import nomad.searchspace.global.exception.ApiException;
import nomad.searchspace.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    //좋아요 등록
    public LikeResponse addLike(PrincipalDetails principalDetails, Long postId){
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOW_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(()-> new ApiException(ErrorCode.SPACE_NOT_FOUND));

        //특정 회원이 해당 게시물에 좋아요를 눌렀는지 확인
        boolean liked = likeRepository.existsByPostAndMember(post, member);

        if(liked){
            throw new ApiException(ErrorCode.LIKE_ALREADY_ADD);
        }

        Likes like = Likes.builder()
                .member(member)
                .post(post)
                .build();

        likeRepository.save(like);
        int updateCount = likeRepository.countByPost(post);

        return LikeResponse.builder()
                .memberId(member.getId())
                .postId(postId)
                .liked(true)
                .likeCount(updateCount)
                .build();
    }

    //좋아요 취소
    public LikeResponse deleteLike(PrincipalDetails principalDetails, Long postId) {
        Member member = memberRepository.findByEmail(principalDetails.getMember().getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOW_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(()-> new ApiException(ErrorCode.SPACE_NOT_FOUND));

        //특정 회원이 해당 게시물에 좋아요를 눌렀는지 확인
        boolean liked = likeRepository.existsByPostAndMember(post, member);
        if(!liked){
            throw new ApiException(ErrorCode.LIKE_ALREADY_CANCEL);
        }

        likeRepository.deleteByMemberAndPost(member,post);
        int updateCount = likeRepository.countByPost(post);

        return LikeResponse.builder()
                .memberId(member.getId())
                .postId(postId)
                .liked(false)
                .likeCount(updateCount)
                .build();
    }

}
