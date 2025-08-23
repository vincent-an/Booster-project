package fortuneCookie.booster.domain.borad.service;

import fortuneCookie.booster.domain.borad.dto.response.PostLikeResponse;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.PostLike;
import fortuneCookie.booster.domain.borad.repository.PostLikeRepository;
import fortuneCookie.booster.domain.borad.repository.PostRepository;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostLikeResponse likeButton(Long postId, String email) {
        log.info("좋아요 작업 요청 - 게시글 ID: {}, 사용자: {}", postId, email);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(currentUser, post);

        boolean isLiked;

        if (existingLike.isPresent()) {
            // 이미 좋아요가 있으면 삭제
            postLikeRepository.delete(existingLike.get());
            isLiked = false;
            log.info("좋아요 취소 - 게시글 ID: {}, 사용자: {}", postId, email);
        } else {
            // 좋아요가 없으면 추가
            PostLike newLike = PostLike.builder()
                    .user(currentUser)
                    .post(post)
                    .build();
            postLikeRepository.save(newLike);
            isLiked = true;
            log.info("좋아요 추가 - 게시글 ID: {}, 사용자: {}", postId, email);
        }

        // 현재 좋아요 수 조회
        long likeCount = postLikeRepository.countByPost(post);

        return PostLikeResponse.of(postId, isLiked, likeCount);
    }

    @Transactional(readOnly = true)
    public PostLikeResponse getLikeStatus(Long postId, String email) {
        log.info("좋아요 상태 조회 - 게시글 ID: {}, 사용자: {}", postId, email);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 좋아요 상태 확인
        boolean isLiked = postLikeRepository.existsByUserAndPost(currentUser, post);

        // 현재 좋아요 수 조회
        long likeCount = postLikeRepository.countByPost(post);

        return PostLikeResponse.of(postId, isLiked, likeCount);
    }
}
