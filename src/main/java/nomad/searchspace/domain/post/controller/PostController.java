package nomad.searchspace.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.post.DTO.PostDTO;
import nomad.searchspace.domain.post.DTO.PostRequest;
import nomad.searchspace.domain.post.DTO.PostResponse;
import nomad.searchspace.domain.post.service.PostService;
import nomad.searchspace.global.auth.PrincipalDetails;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name="Post API")
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService service;

    //좋아요 top10 게시물 가져오기
    @Operation(summary = "Redis ZSET에서 좋아요 Top10 조회", description = "좋아요 상위 10개의 게시물을 캐시에서 가져오는 api")
    @GetMapping("/get/top10")
    public ResponseEntity<List<PostResponse>> getTop10(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<PostResponse> top10 = service.getTop10FromRedis(principalDetails);
        return ResponseEntity.ok(top10);
    }

    //전체 리스트 가져오기(페이지)
    @Operation(summary = "전체 리스트 불러오기(페이징)", description = "전체 post 리스트를 가지고 오고 키워드 검색이 가능합니다 페이징처리되어있는 api 입니다.")
    @GetMapping("/get/pageList")
    public ResponseEntity<Page<PostResponse>> getPostList(@RequestParam(defaultValue = "1") @Nullable int page,
                                                          @RequestParam(required = false) String keyword,@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Page<PostResponse> response = service.getPostList(page, keyword, principalDetails);
        return ResponseEntity.ok(response);
    }

    //전체 리스트 가져오기(커서)
    @Operation(summary = "전체 및 검색 리스트 불러오기(커서)", description = "전체 post 리스트를 가지고 오고 다양한 검색이 가능합니다 커서기반 api 입니다.")
    @GetMapping("/get/cursorList")
    public ResponseEntity<List<PostResponse>> getPosts (@ModelAttribute PostRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<PostResponse> response = service.getPostsByCursor(request, principalDetails);
        return ResponseEntity.ok(response);
    }

    //특정 id로 상세정보 가져오기
    @Operation(summary = "게시물의 상세정보 가져오기", description = "특정 id 값으로 게시물의 상세한 정보를 가지고 올수 있는 api 입니다.")
    @GetMapping("/get/postInfo")
    public ResponseEntity<PostResponse> getPost(@RequestParam Long postId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PostResponse response = service.getPost(postId, principalDetails);
        return ResponseEntity.ok(response);
    }

    //게시물 생성
    @Operation(summary = "게시물 생성 요청", description = "게시물 생성을 요청하는 api 입니다.")
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(@RequestPart PostDTO dto, @RequestPart List<MultipartFile> images, @AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException, ParseException {
        PostResponse response = service.create(dto, images, principalDetails);
        return ResponseEntity.ok(response);
    }

    //게시물 삭제
    @Operation(summary = "게시물 삭제", description = "관리자가 없어진 혹은 부적절한 게시물을 삭제하는 api 입니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<PostResponse> deletePost(@RequestParam Long postId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        PostResponse response = service.delete(postId, principalDetails);
        return ResponseEntity.ok(response);
    }

}
