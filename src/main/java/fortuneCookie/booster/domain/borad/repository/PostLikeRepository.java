package fortuneCookie.booster.domain.borad.repository;

import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.PostLike;
import fortuneCookie.booster.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    // 특정 게시글 좋아요 여부 확인 (boolean)
    boolean existsByUserAndPost(User user, Post post);

    // 특정 게시글의 좋아요 수 조회
    long countByPost(Post post);

    // 게시글 좋아요 삭제
    void deleteByUserAndPost(User user, Post post);
}
