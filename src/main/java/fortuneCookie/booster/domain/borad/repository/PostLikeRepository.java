package fortuneCookie.booster.domain.borad.repository;

import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.PostLike;
import fortuneCookie.booster.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
    long countByPost(Post post);
}
