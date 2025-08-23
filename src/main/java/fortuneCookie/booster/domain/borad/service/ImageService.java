package fortuneCookie.booster.domain.borad.service;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import fortuneCookie.booster.global.config.S3Config;
import fortuneCookie.booster.global.exception.ImageDeleteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final S3Config s3Config;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${local.image-location}")
    private String localLocation;
    // 우분투 경로 "/home/ubuntu/images/"

    private static final Set<String> ALLOWED_CT = Set.of("image/jpeg","image/png","image/webp","image/gif");

    //이미지 업로드 기능
    public String imageUploadProcess(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CT.contains(ct)) {
            throw new IllegalArgumentException("이미지 형식(jpeg/png/webp/gif)만 업로드 가능합니다.");
        }

        // extractExt를 통해 확장자 검증 및 추출
        String ext = extractExt(file);
        String uuidFileName = UUID.randomUUID() + ext;

        // 지정된 로컬 경로에 저장
        String localPath = localLocation + uuidFileName;
        File localFile = new File(localPath);
        file.transferTo(localFile);

        try {
            PutObjectRequest put = new PutObjectRequest(bucket, uuidFileName, localFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            s3Config.amazonS3Client().putObject(put);
            String s3Url = s3Config.amazonS3Client().getUrl(bucket, uuidFileName).toString();
            return s3Url;
        } finally {
            // S3 업로드 완료 후 로컬 파일 삭제
            if (localFile.exists() && !localFile.delete()) {
                log.warn("로컬 파일 삭제 실패: {}", localFile.getAbsolutePath());
            }
        }
    }



    // 이미지 삭제
    public void deleteImages(List<String> imageKeys) {
        // 이미지 삭제 실패 시 이미지 url 리스트에 담음
        List<String> failedKeys = new ArrayList<>();

        for (String key : imageKeys) {
            try {
                s3Config.amazonS3Client().deleteObject(bucket, key);
                log.info("이미지 삭제 성공: {}", key);
            } catch (Exception e) {
                log.error("이미지 삭제 실패: key = {}, error = {}", key, e.getMessage());
                failedKeys.add(key);
            }
        }
        //실패 리스트가 존재 할 경우
        if (!failedKeys.isEmpty()) {
            throw new ImageDeleteException("이미지 삭제 실패: " + failedKeys);
        }
    }


    // 이미지 수정
    public List<String> updateImageAtIndex(List<String> currentImageUrls, int replaceIndex,
                                           MultipartFile newImageFile) throws IOException {
        if (replaceIndex < 0 || replaceIndex >= currentImageUrls.size()) {
            throw new IllegalArgumentException("유효하지 않은 이미지 인덱스입니다." +
                    " (인덱스: " + replaceIndex + ", 리스트 크기: " + currentImageUrls.size() + ")");
        }

        // 기존 이미지 URL에서 key 추출
        String oldImageUrl = currentImageUrls.get(replaceIndex);
        String oldImageKey = extractKeyFromUrl(oldImageUrl);

        log.info("이미지 교체 시작 - 기존 키: {}, 인덱스: {}", oldImageKey, replaceIndex);

        // 새 이미지 업로드
        String newImageUrl = imageUploadProcess(newImageFile);

        try {
            // 기존 이미지 S3에서 삭제
            s3Config.amazonS3Client().deleteObject(bucket, oldImageKey);
            log.info("기존 이미지 삭제 성공: {}", oldImageKey);
        } catch (Exception e) {
            log.warn("기존 이미지 삭제 실패: key = {}, error = {}", oldImageKey, e.getMessage());
            // 새 이미지는 이미 업로드되었으므로 계속 진행
        }

        // 리스트 복사 및 특정 인덱스 교체
        List<String> updatedUrls = new ArrayList<>(currentImageUrls);
        updatedUrls.set(replaceIndex, newImageUrl);

        log.info("이미지 교체 완료 - 새 URL: {}, 인덱스: {}", newImageUrl, replaceIndex);
        return updatedUrls;
    }



    // 확장자 검증 메서드
    private String extractExt(MultipartFile file) {
        String ct = file.getContentType();
        switch (ct) {
            case "image/jpeg": return "jpg";
            case "image/png": return "png";
            case "image/webp": return "webp";
            case "image/gif": return "gif";
        }
        String name = file.getOriginalFilename();
        int idx = name != null ? name.lastIndexOf('.') : -1;
        return (idx > 0 && idx < name.length() - 1) ? name.substring(idx + 1).toLowerCase() : "bin";
    }


    // URL에서 S3 키(파일명) 추출
    private String extractKeyFromUrl(String imageUrl) {
        try {
            return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 이미지 URL 형식입니다: " + imageUrl);
        }
    }

    // 프로필 이미지 전용 업로드 메서드
    public String uploadProfileImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CT.contains(ct)) {
            throw new IllegalArgumentException("이미지 형식(jpeg/png/webp/gif)만 업로드 가능합니다.");
        }

        // 프로필 이미지는 파일명에 "profile-" 접두사 추가
        String ext = extractExt(file);
        String uuidFileName = "profile-" + UUID.randomUUID() + "." + ext;

        // 지정된 로컬 경로에 저장
        String localPath = localLocation + uuidFileName;
        File localFile = new File(localPath);
        file.transferTo(localFile);

        try {
            PutObjectRequest put = new PutObjectRequest(bucket, uuidFileName, localFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            s3Config.amazonS3Client().putObject(put);
            String s3Url = s3Config.amazonS3Client().getUrl(bucket, uuidFileName).toString();

            log.info("프로필 이미지 업로드 완료: {}", s3Url);
            return s3Url;
        } finally {
            // S3 업로드 완료 후 로컬 파일 삭제
            if (localFile.exists() && !localFile.delete()) {
                log.warn("로컬 파일 삭제 실패: {}", localFile.getAbsolutePath());
            }
        }
    }

    // 프로필 이미지 삭제 메서드
    public void deleteProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.contains("default-profile.png")) {
            return;
        }

        try {
            String key = extractKeyFromUrl(imageUrl);
            s3Config.amazonS3Client().deleteObject(bucket, key);
            log.info("프로필 이미지 삭제 성공: {}", key);
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 실패: {}", imageUrl, e);
        }
    }
}
