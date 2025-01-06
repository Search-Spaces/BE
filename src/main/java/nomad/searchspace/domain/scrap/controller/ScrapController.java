package nomad.searchspace.domain.scrap.controller;

import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.scrap.service.ScrapService;
import nomad.searchspace.global.auth.PrincipalDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/scraps")
@RestController
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping("")
    public ResponseEntity editScrap(@RequestParam("postId") Long postId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        scrapService.editScrap(postId, principalDetails);
        return ResponseEntity.ok().build();
    }

}
