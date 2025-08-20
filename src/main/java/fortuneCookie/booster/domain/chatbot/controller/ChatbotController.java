package fortuneCookie.booster.domain.chatbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/booster/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    private static final String prompt = """
            당신은 을지대학교 학생들을 위한 공지사항 안내 챗봇입니다.
            제공된 문서를 바탕으로 학생들의 질문에 정확하고 친절하게 답변해주세요.
            
            답변 규칙:
            1. 정확한 답변을 위해 문서 정보만을 바탕으로 답변하세요.
            2. 정보가 부족하거나 찾을 수 없다면, "해당 정보를 찾을 수 없습니다. 교무팀에 직접 문의해주세요"라고 답변하세요.
            3. 답변은 한국어로, 존댓말을 사용하여 친근하게 해주세요.
            4. 중요한 날짜나 마감일이 있다면 강조해서 알려주세요.
            
            시간대 변환 정보:
            - 오전 9시 = 주1
            - 오전 10시 = 주2
            - 오전 11시 = 주3
            - 오후 12시(정오) = 주4
            - 오후 1시 = 주5
            - 오후 2시 = 주6
            - 오후 3시 = 주7
            - 오후 4시 = 주8
            - 오후 5시 = 주9
            
            사용자가 수강과목 관련해서 요일과 시간대(오전/오후 X시)를 언급하면,
            사용자가 언급한 요일에 들을 수 있는 과목과 사용자가 언급한 시간대부터 시작하는 과목들로
            위의 변환표를 참고하여 해당 주차(주X)로 문서에서 정확히 찾아서 답변해주세요.
            
            
            질문: {input}
            
            관련 공지사항:
            {documents}
            
            답변:
            """;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, Object> request) {
        try {
            String question = (String) request.get("question");
            log.info("을지대학교 챗봇 질문 수신: {}", question);

            // 관련 문서 검색
            List<Document> similarDocuments = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(5)
                            .similarityThreshold(0.7)
                            .build()
            );

            log.info("검색된 관련 문서 수: {}", similarDocuments.size());

            // 검색된 문서들 정리
            String documents = formatDocuments(similarDocuments);
            List<String> sources = extractSources(similarDocuments);

            //프롬프트 생성 및 ai 호출
            PromptTemplate template = new PromptTemplate(prompt);
            Map<String, Object> promptParameters =new HashMap<>();
            promptParameters.put("input", question);
            promptParameters.put("documents", documents);

            String answer = chatModel
                    .call(template.create(promptParameters))
                    .getResult()
                    .getOutput()
                    .getText();

            Map<String, Object> response = new HashMap<>();
            response.put("answer", answer);
            response.put("question", question);
            response.put("sources", sources);
            response.put("timestamp", LocalDateTime.now());

            log.info("챗봇 답변 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("챗봇 처리 중 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("answer", "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            errorResponse.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    private String formatDocuments(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    String source = doc.getMetadata().get("source").toString();
                    String category = doc.getMetadata().get("category").toString();
                    String content = doc.getText();
                    return String.format("[출처: %s (%s)]\n%s", source, category, content);
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private List<String> extractSources(List<Document> documents) {
        return documents.stream()
                .map(doc -> doc.getMetadata().get("source").toString())
                .distinct()
                .collect(Collectors.toList());
    }
}
