package fortuneCookie.booster.domain.borad.service;

import fortuneCookie.booster.domain.borad.dto.request.PostRequest;
import fortuneCookie.booster.domain.borad.dto.response.HomeIntroResponse;
import fortuneCookie.booster.domain.borad.dto.response.PostIntroResponse;
import fortuneCookie.booster.domain.borad.dto.response.PostResponse;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import fortuneCookie.booster.domain.borad.repository.PostRepository;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<HomeIntroResponse> getMainPage() {
        List<Post> posts = postRepository.findTop10ByOrderByLikeCountDesc();

        List<Post> top10Posts = posts.stream()
                .limit(10)
                .collect(Collectors.toList());

        log.info("인기 게시글 조회 완료 - {} 개", top10Posts.size());

        return top10Posts.stream()
                .map(HomeIntroResponse::from)
                .collect(Collectors.toList());
    }

    // 게시글 작성
    @Transactional
    public PostResponse createPost(PostRequest request, String email) {
        log.info("게시글 작성 요청 - 사용자: {}, 제목: {}", email, request.getTitle());

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .isAnonymous(request.getIsAnonymous())
                .introImgURL(request.getIntroImgUrl())
                .imgURL(request.getImgUrls())
                .user(currentUser)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("게시글 작성 완료 - ID: {}", savedPost.getPostId());

        return PostResponse.from(savedPost, currentUser, "게시글 등록 성공");
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostRequest request, String email) {
        log.info("게시글 수정 요청 - ID: {}, 사용자: {}", postId, email);

        // 게시글과 사용자 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 작성자 권한 확인
        if (!post.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("게시글을 수정할 권한이 없습니다.");
        }

        // 수정한 것들만 변경
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            post.setCategory(request.getCategory());
        }
        if (request.getIsAnonymous() != null) {
            post.setIsAnonymous(request.getIsAnonymous());
        }
        if (request.getIntroImgUrl() != null) {
            post.setIntroImgURL(request.getIntroImgUrl());
        }
        if (request.getImgUrls() != null) {
            post.setImgURL(request.getImgUrls());
        }

        Post savedPost = postRepository.save(post);

        log.info("게시글 수정 완료 - ID: {}", savedPost.getPostId());

        return PostResponse.from(savedPost, currentUser, "게시글 수정 성공");
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, String email) {
        log.info("게시글 삭제 요청 - ID: {}, 사용자: {}", postId, email);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(post);

        log.info("게시글 삭제 완료 - ID: {}", postId);
    }

    // 상세 게시글 조회
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId, String email) {
        log.info("게시글 상세 조회 - ID: {}, 사용자: {}", postId, email);

        // 게시글 조회 (댓글, 좋아요 정보 포함)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 현재 사용자 조회
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        log.info("게시글 조회 완료 - ID: {}, 제목: {}", postId, post.getTitle());

        return PostResponse.from(post, currentUser, "게시글 조회 성공");

    }

    // 전체 게시글 조회
    @Transactional(readOnly = true)
    public List<PostIntroResponse> getAllPostsIntro() {
        log.info("전체 게시판 인트로 조회 요청");

        // 최신순으로 모든 게시글 조회
        List<Post> posts = postRepository.findAllByOrderByCreatePostTimeDesc();

        return posts.stream()
                .map(PostIntroResponse::from)
                .collect(Collectors.toList());
    }

    // 카테고리별 조회
    @Transactional(readOnly = true)
    public List<PostIntroResponse> getPostsIntroByCategory(Category category) {
        log.info("{} 카테고리 게시판 인트로 조회 요청", category);

        // 특정 카테고리의 게시글만 최신순으로 조회
        List<Post> posts = postRepository.findByCategoryOrderByCreatePostTimeDesc(category);

        return posts.stream()
                .map(PostIntroResponse::from)
                .collect(Collectors.toList());
    }

    // 검색기능
    @Transactional(readOnly = true)
    public List<PostIntroResponse> searchPosts(String keyword) {
        log.info("게시글 검색 요청 - 키워드: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("검색 키워드가 비어있습니다.");
            return new ArrayList<>();
        }

        // 키워드 전처리 (앞뒤 공백 제거)
        String trimmedKeyword = keyword.trim();

        // 제목과 내용 둘 중 해당되는 키워드로 검색
        List<Post> searchResults = postRepository
                .findByTitleContainingOrContentContainingOrderByCreatePostTimeDesc(
                        trimmedKeyword, trimmedKeyword);

        log.info("검색 완료 - 키워드: '{}', 결과 수: {}", trimmedKeyword, searchResults.size());

        return searchResults.stream()
                .map(PostIntroResponse::from)
                .collect(Collectors.toList());
    }
}
