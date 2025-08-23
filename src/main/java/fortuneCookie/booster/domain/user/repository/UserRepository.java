package fortuneCookie.booster.domain.user.repository;

import fortuneCookie.booster.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 본인 제외하고 이메일 겹치는지 확인
    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);
}
