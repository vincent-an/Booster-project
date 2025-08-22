package fortuneCookie.booster.domain.chatbot.repository;

import fortuneCookie.booster.domain.chatbot.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByUser_UserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);
}
