package fortuneCookie.booster.domain.borad.repository;

import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 전체 게시글 최신순 조회
//    @Query("SELECT p FROM Post p ORDER BY p.createPostTime DESC")
    List<Post> findAllByOrderByCreatePostTimeDesc();

    // 카테고리별 게시글 최신순 조회
//    @Query("SELECT p FROM Post p WHERE p.category = :category ORDER BY p.createPostTime DESC")
    List<Post> findByCategoryOrderByCreatePostTimeDesc(Category category);

    // 좋아요 수 상위 10개 조회
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "ORDER BY SIZE(p.postLikes) DESC, p.createPostTime DESC")
    List<Post> findTop10ByOrderByLikeCountDesc();
}
