package fortuneCookie.booster.domain.borad.controller;

import fortuneCookie.booster.domain.borad.dto.response.PostLikeResponse;
import fortuneCookie.booster.domain.borad.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booster")
@RequiredArgsConstructor
@Slf4j
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 좋아요 누르기 / 취소
    @PostMapping("/{post_id}/like")
    public ResponseEntity<PostLikeResponse> likeButton(
            @PathVariable("post_id") Long postId, Authentication authentication) {

        try {
            String email = authentication.getName();
            PostLikeResponse response = postLikeService.likeButton(postId, email);

            log.info("좋아요 누르기 성공 - 게시글 ID: {}, 상태: {}", postId, response.getIsLiked());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("좋아요 누르기 실패 - 게시글 ID: {}", postId, e);
            throw new RuntimeException("좋아요 처리에 실패했습니다.");
        }
    }

    // 내가 좋아요를 눌렀는지 조회하는 메서드
    @GetMapping("/{post_id}/like")
    public ResponseEntity<PostLikeResponse> getLikeStatus(
            @PathVariable("post_id") Long postId,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            PostLikeResponse response = postLikeService.getLikeStatus(postId, email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("좋아요 상태 조회 실패 - 게시글 ID: {}", postId, e);
            throw new RuntimeException("좋아요 상태 조회에 실패했습니다.");
        }
    }
}
