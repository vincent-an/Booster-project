package fortuneCookie.booster.domain.board.repository;

import fortuneCookie.booster.domain.board.entity.Post;
import fortuneCookie.booster.domain.board.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByPostId(Long postId);

    Page<Post> findByCategory(Category category, Pageable pageable);

    @Query("select p from Post p " +
            "where (:q is null or lower(p.title) like lower(concat('%', :q, '%')) " +
            "   or lower(p.content) like lower(concat('%', :q, '%'))) " +
            "and (:category is null or p.category = :category)")
    Page<Post> search(@Param("q") String q, @Param("category") Category category, Pageable pageable);
}
