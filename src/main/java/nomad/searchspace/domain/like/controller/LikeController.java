package nomad.searchspace.domain.like.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.like.DTO.LikeResponse;
import nomad.searchspace.domain.like.service.LikeService;
import nomad.searchspace.global.auth.PrincipalDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="Like API")
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @Operation(summary = "좋아요 클릭시 좋아요 등록", description = "좋아요 클릭시 좋아요 등록하는 api 입니다.")
    @PostMapping("/create")
    public ResponseEntity<LikeResponse> addLike(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam Long postId) {
        LikeResponse response = likeService.addLike(principalDetails,postId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좋아요 클릭시 좋아요 취소", description = "좋아요 클릭시 좋아요 삭제하는 api 입니다")
    @DeleteMapping("/delete")
    public ResponseEntity<LikeResponse> deleteLike(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam Long postId) {
        LikeResponse response = likeService.deleteLike(principalDetails, postId);
        return ResponseEntity.ok(response);
    }

}
