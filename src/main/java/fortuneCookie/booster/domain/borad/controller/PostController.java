package fortuneCookie.booster.domain.borad.controller;

import fortuneCookie.booster.domain.borad.dto.PostDtos.*;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import fortuneCookie.booster.domain.borad.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({
        "/booster/generalBoard",
        "/booster/freeBoard",
        "/booster/promoBoard",
        "/booster/infoBoard",
        "/booster/tmiBoard"
})
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Response>> list(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "category", required = false) Category category,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "me", required = false) Long me,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        if (path.contains("freeBoard")) category = Category.FREE;
        else if (path.contains("promoBoard")) category = Category.PROMO;
        else if (path.contains("infoBoard")) category = Category.INFO;
        else if (path.contains("tmiBoard")) category = Category.TMI;
        return ResponseEntity.ok(postService.list(q, category, page, size, me));
    }

    // 상세 조회
    @GetMapping("/{post_id}")
    public ResponseEntity<Response> get(
            @PathVariable(name = "post_id") Long id,
            @RequestParam(name = "me", required = false) Long me
    ) {
        return ResponseEntity.ok(postService.get(id, me));
    }

    // 생성
    @PostMapping
    public ResponseEntity<Response> create(@RequestBody CreateRequest req) {
        return ResponseEntity.ok(postService.create(req));
    }

    // 수정  (서비스: update(id, req, me))
    @PatchMapping("/{post_id}")
    public ResponseEntity<Response> update(
            @PathVariable(name = "post_id") Long id,
            @RequestBody UpdateRequest req,
            @RequestParam(name = "me") Long me
    ) {
        return ResponseEntity.ok(postService.update(id, req, me));
    }

    // 삭제  (서비스: delete(id, me))
    @DeleteMapping("/{post_id}")
    public ResponseEntity<Void> delete(
            @PathVariable(name = "post_id") Long id,
            @RequestParam(name = "me") Long me
    ) {
        postService.delete(id, me);
        return ResponseEntity.noContent().build();
    }

    // 좋아요
    @PostMapping("/{id}/like")
    public ResponseEntity<Integer> like(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "me") Long me
    ) {
        return ResponseEntity.ok(postService.like(id, me));
    }

    // 좋아요 취소
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Integer> unlike(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "me") Long me
    ) {
        return ResponseEntity.ok(postService.unlike(id, me));
    }

    // 댓글 추가
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> comment(
            @PathVariable(name = "id") Long id,
            @RequestBody CommentCreateRequest req
    ) {
        return ResponseEntity.ok(postService.addComment(id, req));
    }
}
