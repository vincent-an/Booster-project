package fortuneCookie.booster.domain.user.service;

import fortuneCookie.booster.domain.user.dto.CustomUserDetails;
import fortuneCookie.booster.domain.user.dto.LoginUserDTO;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user != null) {
            //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
//            LoginUserDTO dto = new LoginUserDTO(user.getEmail(), user.getRole(), user.getUserId());
            return new CustomUserDetails(user);
        }

        return null;
    }
}
