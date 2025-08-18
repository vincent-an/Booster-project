package fortuneCookie.booster.domain.user.service;

import fortuneCookie.booster.domain.user.dto.JoinRequest;
import fortuneCookie.booster.domain.user.dto.UserResponse;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.entity.enums.Role;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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
}
