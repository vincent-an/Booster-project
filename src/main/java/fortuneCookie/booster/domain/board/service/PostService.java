package fortuneCookie.booster.domain.board.service;

import fortuneCookie.booster.domain.board.dto.*;
import fortuneCookie.booster.domain.board.entity.Comment;
import fortuneCookie.booster.domain.board.entity.Post;
import fortuneCookie.booster.domain.board.entity.PostLike;
import fortuneCookie.booster.domain.board.entity.enums.Category;
import fortuneCookie.booster.domain.board.repository.CommentRepository;
import fortuneCookie.booster.domain.board.repository.PostLikeRepository;
import fortuneCookie.booster.domain.board.repository.PostRepository;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    // 생성
    public PostResponse create(PostCreateRequest req) {
        User author = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        Post post = Post.of(
                req.getTitle(),
                req.getContent(),
                req.getCategory(),
                req.getAnonymous(),
                author
        );

        Post saved = postRepository.save(post);
        return PostResponse.from(saved, false, Collections.emptyList());
    }

    // 목록 (검색/카테고리)
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> list(String q, Category category, int page, int size, String me) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> result = postRepository.search(q, category, pageable);

        List<PostResponse> content = result.getContent().stream()
                .map(p -> PostResponse.from(p, likedBy(me, p), Collections.emptyList()))
                .collect(Collectors.toList());

        return PageResponse.<PostResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    // 상세
    @Transactional(readOnly = true)
    public PostResponse get(Long id, String me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        List<CommentResponse> commentDtos = commentRepository.findByPost(p).stream()
                .map(CommentResponse::of)
                .collect(Collectors.toList());

        return PostResponse.from(p, likedBy(me, p), commentDtos);
    }

    // 수정
    public PostResponse update(Long id, PostUpdateRequest req, String me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        // 작성자 검증: 이메일(me)로 사용자 조회 후 userId 비교
        User editor = userRepository.findByEmail(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));
        if (p.getUser() == null || !p.getUser().getUserId().equals(editor.getUserId())) {
            throw new ResponseStatusException(FORBIDDEN, "only author can edit");
        }

        if (req.getTitle() != null) p.setTitle(req.getTitle());
        if (req.getContent() != null) p.setContent(req.getContent());
        if (req.getCategory() != null) p.setCategory(req.getCategory());
        if (req.getAnonymous() != null) p.setIsAnonymous(req.getAnonymous());

        return PostResponse.from(p, likedBy(me, p), Collections.emptyList());
    }

    // 삭제
    public void delete(Long id, String me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        User caller = userRepository.findByEmail(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));
        if (p.getUser() == null || !p.getUser().getUserId().equals(caller.getUserId())) {
            throw new ResponseStatusException(FORBIDDEN, "only author can delete");
        }

        postRepository.delete(p);
    }

    // 좋아요
    public int like(Long id, String me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        User u = userRepository.findByEmail(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        if (postLikeRepository.existsByUserAndPost(u, p)) {
            return (int) postLikeRepository.countByPost(p);
        }
        postLikeRepository.save(new PostLike(u, p));
        return (int) postLikeRepository.countByPost(p);
    }

    // 좋아요 취소
    public int unlike(Long id, String me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        User u = userRepository.findByEmail(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        postLikeRepository.findByUserAndPost(u, p).ifPresent(postLikeRepository::delete);
        return (int) postLikeRepository.countByPost(p);
    }

    // 댓글 추가
    public CommentResponse addComment(Long postId, CommentCreateRequest req) {
        Post p = postRepository.findByPostId(postId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        User u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        Comment saved = commentRepository.save(
                Comment.of(req.getContent(), req.getAnonymous(), u, p)
        );
        return CommentResponse.of(saved);
    }

    // 좋아요 여부
    private boolean likedBy(String me, Post p) {
        if (me == null) return false;
        return userRepository.findByEmail(me)
                .map(u -> postLikeRepository.existsByUserAndPost(u, p))
                .orElse(false);
    }
}
