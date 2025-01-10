package nomad.searchspace.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.member.dto.AdditionalMemberInfoRequest;
import nomad.searchspace.domain.member.dto.MemberResponse;
import nomad.searchspace.domain.member.service.MemberService;
import nomad.searchspace.global.auth.PrincipalDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 API")
@RequestMapping("/api/member")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 가입 후 추가 정보 기입", description = "추가 정보 기입을 위한 api 입니다.")
    @PutMapping()
    public ResponseEntity<String> updateMemberInfo(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                   @RequestBody @Validated AdditionalMemberInfoRequest additionalMemberInfoRequest) {
        memberService.updateMember(principalDetails, additionalMemberInfoRequest);

        return ResponseEntity.ok().body("update success");
    }

    @Operation(summary = "마이페이지")
    @GetMapping
    public ResponseEntity<MemberResponse> getMemberInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok().body(memberService.getMemberInfo(principalDetails));
    }


    @PatchMapping("/logout")
    public ResponseEntity logout(@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request) {

        memberService.logout(principalDetails, request);

        return ResponseEntity.ok().body("logout success");
    }

}
