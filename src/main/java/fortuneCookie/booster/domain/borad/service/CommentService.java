package fortuneCookie.booster.domain.borad.service;

import fortuneCookie.booster.domain.borad.dto.request.CommentRequest;
import fortuneCookie.booster.domain.borad.dto.response.CommentResponse;
import fortuneCookie.booster.domain.borad.entity.Comment;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.repository.CommentRepository;
import fortuneCookie.booster.domain.borad.repository.PostRepository;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request, String email) {
        log.info("댓글 작성 요청 - 게시글 ID: {}, 사용자: {}", postId, email);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        // 댓글 생성
        Comment comment = new Comment(
                request.getContent(),
                request.getIsAnonymous(),
                currentUser,
                post
        );
        Comment savedComment = commentRepository.save(comment);

        // 🔍 디버깅용 로그 추가
        log.info("댓글 작성자 ID: {}", savedComment.getUser().getUserId());
        log.info("게시글 작성자 ID: {}", post.getUser().getUserId());
        log.info("글쓴이 여부: {}", savedComment.getUser().getUserId().equals(post.getUser().getUserId()));

        log.info("댓글 작성 완료 - 댓글 ID: {}", savedComment.getCommentId());
        return CommentResponse.from(savedComment, currentUser, post);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId, String email) {
        log.info("게시글 댓글 목록 조회 - 게시글 ID: {}", postId);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 댓글 목록 조회
        List<Comment> comments = commentRepository.findByPostIdOrderByCreateCommentTimeAsc(postId);

        return comments.stream()
                .map(comment -> CommentResponse.from(comment, currentUser, post))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, String email) {
        log.info("댓글 삭제 요청 - 댓글 ID: {}, 사용자: {}", commentId, email);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 댓글 조회 (해당 게시글의 댓글인지 확인)
        Comment comment = commentRepository.findByCommentIdAndPost(commentId, post)
                .orElseThrow(() -> new RuntimeException("해당 게시글의 댓글을 찾을 수 없습니다."));


        if (!comment.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);

        log.info("댓글 삭제 완료 - 댓글 ID: {}", commentId);
    }
}
