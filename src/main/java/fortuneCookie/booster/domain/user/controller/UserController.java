package fortuneCookie.booster.domain.user.controller;

import fortuneCookie.booster.domain.user.dto.UserProfileDTO;
import fortuneCookie.booster.domain.user.dto.request.JoinRequest;
import fortuneCookie.booster.domain.user.dto.request.PasswordUpdateRequest;
import fortuneCookie.booster.domain.user.dto.request.UpdateProfileRequest;
import fortuneCookie.booster.domain.user.dto.response.UserResponse;
import fortuneCookie.booster.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/booster")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<UserResponse> join(@RequestBody JoinRequest request) {
        log.info("회원가입 시작 : 이메일={}, 닉네임={}, 학번={}", request.getEmail(),
                request.getNickname(), request.getAdmissionYear());
        UserResponse response = userService.joinProcess(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDTO profile= userService.getMyProfile(email);
        return ResponseEntity.ok(profile);
    }

    // 프로필 이미지 업로드, 수정
    @PostMapping("/profile/image")
    public ResponseEntity<Map<String, String>> updateProfileImage(
            @RequestParam("image") MultipartFile imageFile,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            userService.updateProfileImage(email, imageFile);

            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필 이미지가 성공적으로 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (IOException | RuntimeException e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "이미지 업로드에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 프로필 이미지 삭제(기본 이미지로)
    @DeleteMapping("/profile/image")
    public ResponseEntity<Map<String, String>> resetProfileImage(Authentication authentication) {
        try {
            String email = authentication.getName();
            userService.resetProfileImageToDefault(email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필 이미지가 기본 이미지로 변경되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("프로필 이미지 초기화 실패: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PatchMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                             Authentication authentication) {
        try {
            String email = authentication.getName();
            userService.updateProfile(email, request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필이 성공적으로 수정되었습니다.");

            log.info("프로필 수정 성공 - 사용자: {}", email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("프로필 수정 실패 - 에러: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 🔍 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            userService.updatePassword(email, request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "비밀번호가 성공적으로 변경되었습니다.");

            log.info("비밀번호 변경 성공 - 사용자: {}", email);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("비밀번호 변경 실패 - 에러: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

}
