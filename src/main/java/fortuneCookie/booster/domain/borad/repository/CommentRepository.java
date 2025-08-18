package fortuneCookie.booster.domain.borad.repository;

import fortuneCookie.booster.domain.borad.entity.Comment;
import fortuneCookie.booster.domain.borad.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
}