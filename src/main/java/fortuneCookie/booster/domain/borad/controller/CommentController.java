package fortuneCookie.booster.domain.borad.controller;

import fortuneCookie.booster.domain.borad.dto.request.CommentRequest;
import fortuneCookie.booster.domain.borad.dto.response.CommentResponse;
import fortuneCookie.booster.domain.borad.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booster")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{post_id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable("post_id") Long postId, @RequestBody CommentRequest request,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            CommentResponse response = commentService.createComment(postId, request, email);

            log.info("댓글 작성 성공 - 게시글 ID: {}, 댓글 ID: {}", postId, response.getCommentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("댓글 작성 실패 - 게시글 ID: {}", postId, e);
            throw new RuntimeException("댓글 작성에 실패했습니다.");
        }
    }

    // 특정 게시글 내 모든 댓글 조회
    @GetMapping("/{post_id}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentByPostId(
            @PathVariable("post_id") Long postId, Authentication authentication) {
        try {
            String email = authentication.getName();
            List<CommentResponse> comments = commentService.getCommentsByPostId(postId, email);

            log.info("게시글 댓글 조회 성공 - 게시글 ID: {}, 댓글 수: {}", postId, comments.size());
            return ResponseEntity.ok(comments);

        } catch (Exception e) {
            log.error("댓글 조회 실패 - 게시글 ID: {}", postId, e);
            throw new RuntimeException("댓글 조회에 실패했습니다.");
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{post_id}/comments/{comment_id}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable("post_id") Long postId,
            @PathVariable("comment_id") Long commentId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            commentService.deleteComment(postId, commentId, email);

            log.info("댓글 삭제 성공 - 게시글 ID: {}, 댓글 ID: {}", postId, commentId);

            Map<String, String> response = Map.of(
                    "message", "댓글이 성공적으로 삭제되었습니다.",
                    "comment_id", commentId.toString()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("댓글 삭제 실패 - 댓글 ID: {}", commentId, e);
            throw new RuntimeException("댓글 삭제에 실패했습니다.");
        }
    }
}
