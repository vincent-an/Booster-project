package fortuneCookie.booster.domain.board.repository;

import fortuneCookie.booster.domain.board.entity.Comment;
import fortuneCookie.booster.domain.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
}
