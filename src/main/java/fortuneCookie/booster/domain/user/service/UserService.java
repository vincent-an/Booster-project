package fortuneCookie.booster.domain.user.service;

import fortuneCookie.booster.domain.borad.service.ImageService;
import fortuneCookie.booster.domain.user.dto.UserProfileDTO;
import fortuneCookie.booster.domain.user.dto.request.JoinRequest;
import fortuneCookie.booster.domain.user.dto.request.PasswordUpdateRequest;
import fortuneCookie.booster.domain.user.dto.request.UpdateProfileRequest;
import fortuneCookie.booster.domain.user.dto.response.UserResponse;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.entity.enums.Role;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse joinProcess(JoinRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        if (!request.getEmail().endsWith("@g.eulji.ac.kr")) {
            throw new RuntimeException("제대로 된 이메일 형식이 아닙니다. 학교 이메일을 사용해 주세요");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .admissionYear(request.getAdmissionYear())
                .gender(request.getGender())
                .department(request.getDepartment())
                .role(Role.fromValue("ROLE_USER"))
                .build();

        userRepository.save(newUser);

        return UserResponse.from(newUser, "회원가입 성공");
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 프로필 이미지가 없으면 기본 이미지 사용
        String profileImageUrl = user.getProfileImageUrl() != null ?
                user.getProfileImageUrl() : "/images/default-profile.png";

        return UserProfileDTO.builder()
                .nickname(user.getNickname())
                .gender(user.getGender())
                .admissionYear(user.getAdmissionYear())
                .department(user.getDepartment())
                .profileImageUrl(profileImageUrl)
                .build();
    }

    // 프로필 이미지 수정 기능
    public void updateProfileImage(String email, MultipartFile imageFile) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기존 이미지 삭제
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().contains("default-profile.png")) {
            imageService.deleteProfileImage(user.getProfileImageUrl());
        }

        // 새 이미지 업로드
        String newImageUrl = imageService.uploadProfileImage(imageFile);
        user.setProfileImageUrl(newImageUrl);

        userRepository.save(user);
        log.info("프로필 이미지 수정 완료 - 사용자: {}", user.getEmail());
    }

    // 기본 이미지로 변경 기능
    public void resetProfileImageToDefault(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기존 이미지 삭제 (기본 이미지가 아닌 경우만)
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().contains("default-profile.png")) {
            imageService.deleteProfileImage(user.getProfileImageUrl());
        }

        // 기본 이미지로 설정
        user.setProfileImageUrl("/images/default-profile.png");
        userRepository.save(user);

        log.info("프로필 이미지 기본값으로 변경 - 사용자: {}", user.getEmail());
    }

    // 프로필 정보 수정
    public void updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 닉네임 중복 체크
        if (!user.getNickname().equals(request.getNickname())) {
            boolean exists = userRepository.existsByNicknameAndUserIdNot(
                    request.getNickname(), user.getUserId());
            if (exists) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
        }

        user.setNickname(request.getNickname());
        user.setGender(request.getGender());
        user.setAdmissionYear(request.getAdmissionYear());
        user.setDepartment(request.getDepartment());

        userRepository.save(user);
        log.info("프로필 수정 완료 - 사용자: {}, 닉네임: {}", user.getEmail(), request.getNickname());
    }

    // 비밀번호 변경 메서드
    public void updatePassword(String email, PasswordUpdateRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);

        userRepository.save(user);
        log.info("비밀번호 변경 완료 - 사용자: {}", user.getEmail());
    }
}
