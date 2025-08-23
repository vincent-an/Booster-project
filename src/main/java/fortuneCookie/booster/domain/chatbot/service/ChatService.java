package fortuneCookie.booster.domain.chatbot.service;

import fortuneCookie.booster.domain.chatbot.entity.Chat;
import fortuneCookie.booster.domain.chatbot.repository.ChatRepository;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public void saveUserMessage(Long userId, String sessionId, String content) {
        log.debug("사용자 메시지 저장 - userId: {}, sessionId: {}", userId, sessionId);
        saveChat(userId, sessionId, content, MessageType.USER);
    }

    public void saveAssistantMessage(Long userId, String sessionId, String content) {
        log.debug("챗봇 메시지 저장 - userId: {}, sessionId: {}", userId, sessionId);
        saveChat(userId, sessionId, content, MessageType.ASSISTANT);
    }

    // 채팅 내용 저장 메서드
    private void saveChat(Long userId, String sessionId, String content, MessageType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("사용자를 찾을 수 없습니다."));

        Chat chat = Chat.builder()
                .user(user)
                .sessionId(sessionId)
                .messageType(type)
                .content(content)
                .build();

        chatRepository.save(chat);
        log.debug("채팅 저장 완료 - type: {}, content length: {}", type, content.length());
    }

    // 멀티턴을 위한 세션 조회 메서드 -> 추후에 entity말고 dto를 사용하는게 낫지 않을까..?!
    @Transactional(readOnly = true)
    public List<Chat> getChats(Long userId, String sessionId) {
        log.debug("채팅 조회 - userId: {}, sessionId: {}", userId, sessionId);
        List<Chat> chats = chatRepository.findByUser_UserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
        log.debug("조회된 채팅 개수: {}", chats.size());

        return chats;
    }

    // 이전 대화를 ai용 프롬프트 문자로 변환하는 메서드
    public String buildChatContext(List<Chat> chats) {
        if (chats == null || chats.isEmpty()) {
            return "이전 대화 내역이 존재하지 않습니다.";
        }

        String context = chats.stream()
                .map(chat -> {
                    String role = chat.getMessageType() == MessageType.USER ? "사용자" : "챗봇";
                    return String.format("%s: %s", role, chat.getContent());
                })
                .collect(Collectors.joining("\n"));

        log.debug("대화 컨텍스트 생성 완료 - 메시지 수: {}, 컨텍스트 길이: {}", chats.size(), context.length());
        return context;
    }

    // 최근 N개의 대화만 가져오기 (성능 최적화용)
    @Transactional(readOnly = true)
    public List<Chat> getRecentChats(Long userId, String sessionId, int limit) {
        List<Chat> allChats = getChats(userId, sessionId);

        // 최근 limit개만 반환
        if (allChats.size() <= limit) {
            return allChats;
        }

        List<Chat> recentChats = allChats.subList(allChats.size() - limit, allChats.size());
        log.debug("최근 채팅 조회 완료 - 전체: {}, 반환: {}", allChats.size(), recentChats.size());
        return recentChats;
    }
}
