package nomad.searchspace.domain.member.dto;

import lombok.Builder;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.scrap.domain.Scrap;

import java.util.ArrayList;
import java.util.List;

public class MemberResponse {

    private String email;
    private String nickname;
    private String gender;
    private String birth;
    private List<Long> scrapList = new ArrayList<>();

    @Builder
    public MemberResponse(Member member) {
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.gender = member.getGender() ? "male" : "female";
        this.birth = member.getBirth();
        this.scrapList = member.getScraps().stream()
                .map(scrap -> scrap.getPost().getPostId())
                .toList();

    }
}
