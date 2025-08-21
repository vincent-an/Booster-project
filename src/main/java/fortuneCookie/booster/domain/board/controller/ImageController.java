package fortuneCookie.booster.domain.board.controller;

import fortuneCookie.booster.domain.board.service.ImageService;
import fortuneCookie.booster.global.exception.ImageDeleteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/booster/image")
public class ImageController {

    private final ImageService imageService;

    //이미지 업로드
    @PostMapping("/upload")
    public Map<String, Object> imageUpload(Authentication authentication,
                                           @RequestParam("upload") List<MultipartFile> files
    ) throws IOException {
        String email = authentication.getName();

        if (files == null || files.isEmpty()) {
            log.warn("업로드 실패 - 파일이 없음, 사용자: {}", email);
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (files.size() > 5) {
            log.warn("업로드 실패 - 파일 개수 초과, 사용자: {}, 요청 파일 수: {}", email, files.size());
            throw new IllegalArgumentException("이미지는 최대 5개까지 업로드할 수 있습니다.");
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = imageService.imageUploadProcess(file);
            imageUrls.add(url);
        }
        // 첫 번째 이미지를 대표로 지정
        Map<String, Object> response = new HashMap<>();
        response.put("imgUrls", imageUrls);
        response.put("introImgUrl", imageUrls.get(0));
        return response;
    }

    // 이미지 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImages(Authentication authentication, @RequestBody List<String> imageKeys) {
        String email = authentication.getName();

        try {
            imageService.deleteImages(imageKeys);
            log.info("이미지 삭제 완료!");
            return ResponseEntity.ok().body("이미지 삭제 완료");
        } catch (ImageDeleteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "삭제 중 알 수 없는 오류가 발생했습니다."));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateImagesAtIndex(Authentication authentication,
                                                 @RequestParam("currentImageUrls") List<String> currentUrls,
                                                 @RequestParam("replaceIndex") int replaceIndex,
                                                 @RequestParam("newImage") MultipartFile newImageFile) {
        String email = authentication.getName();
        log.info("이미지 수정 요청 - 사용자: {}, 인덱스: {}", email, replaceIndex);

        try {
            List<String> updateUrls = imageService.updateImageAtIndex(currentUrls, replaceIndex, newImageFile);

            Map<String, Object> response = new HashMap<>();
            response.put("updatedImageUrls", updateUrls);
            response.put("introImgUrl", updateUrls.get(0));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이미지 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "이미지 수정 중 오류가 발생했습니다."));
        }
    }
}
