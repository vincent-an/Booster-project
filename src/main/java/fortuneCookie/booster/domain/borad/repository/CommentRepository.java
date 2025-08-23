package fortuneCookie.booster.domain.borad.repository;

import fortuneCookie.booster.domain.borad.entity.Comment;
import fortuneCookie.booster.domain.borad.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 댓글 목록 조회 (최신순)
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE c.post.postId = :postId " +
            "ORDER BY c.createCommentTime ASC")
    List<Comment> findByPostIdOrderByCreateCommentTimeAsc(@Param("postId") Long postId);

    // 특정 게시글의 댓글 수
    long countByPost_PostId(Long postId);

    Optional<Comment> findByCommentIdAndPost(Long commentId, Post post);
}
