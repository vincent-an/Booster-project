package fortuneCookie.booster.domain.chatbot.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BoosterDataLoader {

    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    @Value("classpath:/notices/2025-2학기_수강_가이드.pdf")
    private Resource courseGuide;

    @Value("classpath:/notices/2025년_전과제도.pdf")
    private Resource majorChange;

    @Value("classpath:/notices/2025학년도_학부제_안내.pdf")
    private Resource departmentGuide;

    @Value("classpath:/notices/2025학년도 2학기_수강신청과목.pdf")
    private Resource courseRegistration;

    @PostConstruct
    public void init() {
        try {
            Integer count = jdbcClient.sql("select count(*) from vector_store")
                    .query(Integer.class)
                    .single();

            log.info("현재 벡터 스토어 레코드 수: {}", count);

            if (count == 0) {
                log.info("을지대학교 공지사항 데이터 로딩 시작...");
                loadAllNoticeDocuments();
            } else {
                log.info("이미 데이터가 로딩되어 있습니다. 건너뜁니다.");
            }
        } catch (Exception e) {
            log.error("데이터 로딩 확인 중 에러가 발생했습니다.", e);
        }
    }

    private void loadAllNoticeDocuments() {
        // 모든 PDF 파일들을 리스트로 정의 (변경된 변수명 반영)
        List<PDFInfo> pdfFiles = Arrays.asList(
                new PDFInfo(courseGuide, "2025-2학기-수강-가이드.pdf", "course_guide", "수강 가이드"),
                new PDFInfo(majorChange, "2025년-전과제도.pdf", "major_change", "전과/소속변경"),
                new PDFInfo(departmentGuide, "2025학년도-학부제-안내.pdf", "department_guide", "학부제"),
                new PDFInfo(courseRegistration, "2025학년도-2학기-수강신청과목.pdf", "course_registration", "수강신청")
        );

        int totalProcessed = 0;

        // 각 pdf 파일 처리
        for (PDFInfo pdfInfo : pdfFiles) {
            try {
                // 1. pdf 파일 읽기 설정
                PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build();

                // 2. pdf 문서 로드
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfInfo.resource, config);
                List<Document> documents = pdfReader.get();
                log.info("PDF 페이지 수: {} - {}", documents.size(), pdfInfo.displayName);

                // 3. 문서 page 분할
                TokenTextSplitter splitter = new TokenTextSplitter(1000, 300, 5, 4000, true);
                List<Document> splitDocuments = splitter.apply(documents);

                // 4. 메타데이터 추가 (각 pdf별로 구분)
                addMetadataToDocuments(splitDocuments, pdfInfo);

                // 5. 벡터 스토어에 저장 (OpenAI 임베딩 + PGVector 저장)
                vectorStore.accept(splitDocuments);

                totalProcessed += splitDocuments.size();
                log.info("로딩 완료: {} - {} 개 문서 청크 저장됨", pdfInfo.displayName, splitDocuments.size());

                // 각 파일 처리 후 잠시 대기 (OpenAI API 호출 제한 고려)
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("PDF 로딩 실패: {}", pdfInfo.displayName, e);
            }
        }
        log.info("전체 로딩 완료: 총 {} 개 문서 청크가 벡터 데이터베이스에 저장됨", totalProcessed);
    }
    // 신뢰성, 분류, 사용자 경험, 디버깅에 활용되는 메타데이터 추가
    private void addMetadataToDocuments(List<Document> documents, PDFInfo pdfInfo) {
        documents.forEach(doc -> {
            doc.getMetadata().put("source", pdfInfo.displayName);
            doc.getMetadata().put("type", pdfInfo.type);
            doc.getMetadata().put("category", pdfInfo.category);
            doc.getMetadata().put("loaded_at", LocalDateTime.now().toString());
            doc.getMetadata().put("university", "을지대학교");
            doc.getMetadata().put("year", "2025");
        });
    }

    // PDF 정보를 담는 내부 클래스
    private static class PDFInfo {
        final Resource resource;
        final String displayName;
        final String type;
        final String category;

        PDFInfo(Resource resource, String displayName, String type, String category) {
            this.resource = resource;
            this.displayName = displayName;
            this.type = type;
            this.category = category;
        }
    }

}
