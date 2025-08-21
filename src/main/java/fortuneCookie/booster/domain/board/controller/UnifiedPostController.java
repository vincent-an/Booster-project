package fortuneCookie.booster.domain.board.controller;

import fortuneCookie.booster.domain.board.dto.PostCreateRequest;
import fortuneCookie.booster.domain.board.dto.PostResponse;
import fortuneCookie.booster.domain.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/booster/posts")
public class UnifiedPostController {

    private final PostService postService;

    // 게시글 작성 카테고리 추가,,
    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody PostCreateRequest req) {
        // PostService#create는 req.getCategory()를 사용하므로 추가적인 변환/매핑이 필요 없습니다.
        return ResponseEntity.ok(postService.create(req));
    }
}
