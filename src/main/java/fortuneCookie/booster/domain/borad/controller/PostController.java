package fortuneCookie.booster.domain.borad.controller;

import fortuneCookie.booster.domain.borad.dto.request.PostRequest;
import fortuneCookie.booster.domain.borad.dto.response.HomeIntroResponse;
import fortuneCookie.booster.domain.borad.dto.response.PostIntroResponse;
import fortuneCookie.booster.domain.borad.dto.response.PostResponse;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import fortuneCookie.booster.domain.borad.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/booster")
public class PostController {

    private final PostService postService;

    // 메인페이지
    @GetMapping("/home")
    public ResponseEntity<List<HomeIntroResponse>> getMainPage() {
        List<HomeIntroResponse> posts = postService.getMainPage();
        return ResponseEntity.ok(posts);
    }

    // 게시글 작성 메서드
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(Authentication authentication,
                                                   @RequestBody PostRequest request) {
        try {
            String email = authentication.getName();
            PostResponse response = postService.createPost(request, email);

            log.info("게시글 작성 성공 - ID: {}", response.getPostId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("게시글 작성 실패", e);
            throw new RuntimeException("게시글 작성에 실패했습니다.");
        }
    }

    // 게시글 수정 메서드
    @PatchMapping("/edit/{post_id}")
    public ResponseEntity<PostResponse> updatePost(
            Authentication authentication,
            @PathVariable("post_id") Long postId,
            @RequestBody PostRequest request) {
        try {
            String email = authentication.getName();
            PostResponse response = postService.updatePost(postId, request, email);

            log.info("게시글 수정 성공 - ID: {}", postId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 수정 실패 - ID: {}", postId, e);
            throw new RuntimeException("게시글 수정에 실패했습니다.");
        }
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{post_id}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable("post_id") Long postId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            postService.deletePost(postId, email);

            log.info("게시글 삭제 성공 - ID: {}", postId);

            Map<String, String> response = Map.of(
                    "message", "게시글이 성공적으로 삭제되었습니다.",
                    "postId", postId.toString()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 삭제 실패 - ID: {}", postId, e);
            throw new RuntimeException("게시글 삭제에 실패했습니다.");
        }
    }

    // 세부 게시글 조회
    @GetMapping("/{post_id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable("post_id") Long postId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            PostResponse response = postService.getPost(postId, email);

            log.info("게시글 조회 성공 - ID: {}", postId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 조회 실패 - ID: {}", postId, e);
            throw new RuntimeException("게시글을 조회할 수 없습니다.");
        }
    }

    // 전체 게시글 조회
    @GetMapping("post/intro")
    public ResponseEntity<List<PostIntroResponse>> getAllPostsIntro() {
        List<PostIntroResponse> posts = postService.getAllPostsIntro();

        log.info("전체 게시판 조회 완료 - {} 개 게시글", posts.size());
        return ResponseEntity.ok(posts);
    }

    // 카테고리별 게시글 조회
    @GetMapping("post/intro/category/{category}")
    public ResponseEntity<List<PostIntroResponse>> getPostsIntroByCategory(
            @PathVariable Category category) {

        List<PostIntroResponse> posts = postService.getPostsIntroByCategory(category);

        log.info("{} 카테고리 게시판 조회 완료 - {} 개 게시글", category, posts.size());
        return ResponseEntity.ok(posts);
    }

    // 검색 기능
    @GetMapping("/search")
    public ResponseEntity<List<PostIntroResponse>> searchPosts(
            @RequestParam("keyword") String keyword) {

        try {
            List<PostIntroResponse> searchResults = postService.searchPosts(keyword);

            log.info("게시글 검색 성공 - 키워드: '{}', 결과 수: {}", keyword, searchResults.size());
            return ResponseEntity.ok(searchResults);

        } catch (Exception e) {
            log.error("게시글 검색 실패 - 키워드: '{}'", keyword, e);
            throw new RuntimeException("게시글 검색에 실패했습니다.");
        }
    }
}
