package fortuneCookie.booster.domain.user.controller;

import fortuneCookie.booster.domain.user.dto.request.JoinRequest;
import fortuneCookie.booster.domain.user.dto.response.UserResponse;
import fortuneCookie.booster.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
