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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BoosterDataLoader {

    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;


    @Value("classpath:/notices/*.pdf")
    private Resource[] pdfResources;

    @Value("classpath:/notices/*.txt")
    private Resource[] txtResources;

    @PostConstruct
    public void init() {
        try {
            Integer count = jdbcClient.sql("select count(*) from vector_store")
                    .query(Integer.class)
                    .single();

            log.info("현재 벡터 스토어 레코드 수: {}", count);

            if (count == 0) {
                log.info("을지대학교 공지사항 데이터 로딩 시작...");
                loadAllDocuments();
            } else {
                log.info("이미 데이터가 로딩되어 있습니다. 건너뜁니다.");
            }
        } catch (Exception e) {
            log.error("데이터 로딩 확인 중 에러가 발생했습니다.", e);
        }
    }

    private void loadAllDocuments() {
        int totalProcessed = 0;

        // PDF 파일 처리
        totalProcessed += loadPdfDocuments();

        // TXT 파일 처리
        totalProcessed += loadTxtDocuments();

        log.info("전체 로딩 완료: 총 {} 개 문서 청크가 벡터 데이터베이스에 저장됨", totalProcessed);
    }

    // PDF 파일 읽기
    private int loadPdfDocuments() {
        int processed = 0;

        log.info("PDF 파일 로딩 시작 - 총 {}개 파일", pdfResources.length);

        for (Resource resource : pdfResources) {
            try {
                String filename = resource.getFilename();
                String displayName = filename.replace(".pdf", "").replace("_", "-");

                log.info("PDF 처리 중: {}", displayName);

                PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build();

                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource, config);
                List<Document> documents = pdfReader.get();
                log.info("PDF 페이지 수: {} - {}", documents.size(), displayName);

                TokenTextSplitter splitter = new TokenTextSplitter(1000, 300, 5, 4000, true);
                List<Document> splitDocuments = splitter.apply(documents);

                addSimpleMetadata(splitDocuments, displayName);
                vectorStore.accept(splitDocuments);

                processed += splitDocuments.size();
                log.info("PDF 로딩 완료: {} - {} 개 문서 청크 저장됨", displayName, splitDocuments.size());

                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("PDF 로딩 실패: {}", resource.getFilename(), e);
            }
        }

        log.info("PDF 파일 로딩 완료 - {} 개 청크 처리됨", processed);
        return processed;
    }

    // TXT 파일 로딩
    private int loadTxtDocuments() {
        int processed = 0;

        log.info("TXT 파일 로딩 시작 - 총 {}개 파일", txtResources.length);

        for (Resource resource : txtResources) {
            try {
                String filename = resource.getFilename();
                String displayName = filename.replace(".txt", "").replace("_", "-");

                log.info("TXT 처리 중: {}", displayName);

                // TXT 파일 내용 읽기
                String content = readTxtFile(resource);
                log.info("TXT 파일 크기: {} 문자 - {}", content.length(), displayName);

                // Document 생성
                Document document = new Document(content);

                // 문서 분할
                TokenTextSplitter splitter = new TokenTextSplitter(1000, 300, 5, 4000, true);
                List<Document> splitDocuments = splitter.apply(List.of(document));

                // 메타데이터 추가
                addSimpleMetadata(splitDocuments, displayName);

                vectorStore.accept(splitDocuments);

                processed += splitDocuments.size();
                log.info("TXT 로딩 완료: {} - {} 개 문서 청크 저장됨", displayName, splitDocuments.size());

                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("TXT 로딩 실패: {}", resource.getFilename(), e);
            }
        }

        log.info("TXT 파일 로딩 완료 - {} 개 청크 처리됨", processed);
        return processed;
    }

    // TXT 파일 읽기 메서드
    private String readTxtFile(Resource resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            return content.toString();
        }
    }

    // 공통 메타데이터 추가
    private void addSimpleMetadata(List<Document> documents, String displayName) {
        documents.forEach(doc -> {
            doc.getMetadata().put("source", displayName);
            doc.getMetadata().put("loaded_at", LocalDateTime.now().toString());
            doc.getMetadata().put("university", "을지대학교");
        });
    }
}

