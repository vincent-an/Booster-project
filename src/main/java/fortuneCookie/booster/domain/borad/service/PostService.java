package fortuneCookie.booster.domain.borad.service;

import fortuneCookie.booster.domain.borad.dto.PostDtos.*;
import fortuneCookie.booster.domain.borad.entity.Comment;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.PostLike;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import fortuneCookie.booster.domain.borad.repository.CommentRepository;
import fortuneCookie.booster.domain.borad.repository.PostLikeRepository;
import fortuneCookie.booster.domain.borad.repository.PostRepository;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository,
                       PostLikeRepository postLikeRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
    }

    public Response create(CreateRequest req) {
        User author = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        Post post = Post.of(
                req.getTitle(),
                req.getContent(),
                req.getCategory(),
                req.getAnonymous(),
                author
        );

        Post saved = postRepository.save(post);
        return Response.from(saved, false, Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public PageResponse<Response> list(String q, Category category, int page, int size, Long me) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> result = postRepository.search(q, category, pageable);

        List<Response> content = result.getContent().stream()
                .map(p -> Response.from(p, likedBy(me, p), Collections.emptyList()))
                .collect(Collectors.toList());

        return PageResponse.<Response>builder()
                .content(content)
                .page(result.getNumber()).size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst()).last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public Response get(Long id, Long me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        List<CommentResponse> commentDtos = commentRepository.findByPost(p).stream()
                .map(CommentResponse::of)
                .collect(Collectors.toList());

        return Response.from(p, likedBy(me, p), commentDtos);
    }

    public Response update(Long id, UpdateRequest req, Long me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        if (p.getUser() == null || !p.getUser().getUserId().equals(me)) {
            throw new ResponseStatusException(FORBIDDEN, "only author can edit");
        }

        if (req.getTitle() != null) p.setTitle(req.getTitle());
        if (req.getContent() != null) p.setContent(req.getContent());
        if (req.getCategory() != null) p.setCategory(req.getCategory());
        if (req.getAnonymous() != null) p.setIsAnonymous(req.getAnonymous());

        return Response.from(p, likedBy(me, p), Collections.emptyList());
    }

    public void delete(Long id, Long me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        if (p.getUser() == null || !p.getUser().getUserId().equals(me)) {
            throw new ResponseStatusException(FORBIDDEN, "only author can delete");
        }

        postRepository.delete(p);
    }

    public int like(Long id, Long me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        User u = userRepository.findByUserId(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        if (postLikeRepository.existsByUserAndPost(u, p)) {
            return (int) postLikeRepository.countByPost(p);
        }
        postLikeRepository.save(new PostLike(u, p));
        return (int) postLikeRepository.countByPost(p);
    }

    public int unlike(Long id, Long me) {
        Post p = postRepository.findByPostId(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        User u = userRepository.findByUserId(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        postLikeRepository.findByUserAndPost(u, p).ifPresent(postLikeRepository::delete);
        return (int) postLikeRepository.countByPost(p);
    }

    public CommentResponse addComment(Long postId, CommentCreateRequest req) {
        Post p = postRepository.findByPostId(postId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "post not found"));

        User u = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        Comment saved = commentRepository.save(
                Comment.of(req.getContent(), req.getAnonymous(), u, p)
        );
        return CommentResponse.of(saved);
    }

    private boolean likedBy(Long me, Post p) {
        if (me == null) return false;
        return userRepository.findByUserId(me)
                .map(u -> postLikeRepository.existsByUserAndPost(u, p))
                .orElse(false);
    }
}
