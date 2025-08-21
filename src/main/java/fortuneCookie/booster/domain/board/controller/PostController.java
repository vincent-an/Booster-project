package fortuneCookie.booster.domain.board.controller;

import fortuneCookie.booster.domain.board.dto.*;
import fortuneCookie.booster.domain.board.entity.enums.Category;
import fortuneCookie.booster.domain.board.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping({
        "/booster/generalBoard",
        "/booster/freeBoard",
        "/booster/promoBoard",
        "/booster/infoBoard",
        "/booster/tmiBoard"
})
public class PostController {

    private final PostService postService;

    // 목록
    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> list(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "category", required = false) Category category,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "me", required = false) String me,
            HttpServletRequest request
    ) {
        // 경로에 따라 category 강제 설정
        String path = request.getRequestURI();
        if (path.contains("freeBoard")) category = Category.FREE;
        else if (path.contains("promoBoard")) category = Category.PROMO;
        else if (path.contains("infoBoard")) category = Category.INFO;
        else if (path.contains("tmiBoard")) category = Category.TMI;

        return ResponseEntity.ok(postService.list(q, category, page, size, me));
    }

    // 상세
    @GetMapping("/{post_id}")
    public ResponseEntity<PostResponse> get(
            @PathVariable(name = "post_id") Long id,
            @RequestParam(name = "me", required = false) String me
    ) {
        return ResponseEntity.ok(postService.get(id, me));
    }

    // 생성
    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(postService.create(req));
    }

    // 수정
    @PatchMapping("/{post_id}")
    public ResponseEntity<PostResponse> update(
            @PathVariable(name = "post_id") Long id,
            @RequestBody PostUpdateRequest req,
            @RequestParam(name = "me") String me
    ) {
        return ResponseEntity.ok(postService.update(id, req, me));
    }

    // 삭제
    @DeleteMapping("/{post_id}")
    public ResponseEntity<Void> delete(
            @PathVariable(name = "post_id") Long id,
            @RequestParam(name = "me") String me
    ) {
        postService.delete(id, me);
        return ResponseEntity.noContent().build();
    }

    // 좋아요
    @PostMapping("/{id}/like")
    public ResponseEntity<Integer> like(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "me") String me
    ) {
        return ResponseEntity.ok(postService.like(id, me));
    }

    // 좋아요 취소
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Integer> unlike(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "me") String me
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
