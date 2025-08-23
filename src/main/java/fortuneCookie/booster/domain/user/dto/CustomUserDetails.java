package fortuneCookie.booster.domain.user.dto;

import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.entity.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final String email;
    private final String passwordHash; // 로그인 시에만 사용
    private final Role role;

    // 로그인용: 엔티티 기반 (해시 포함)
    public CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.passwordHash = user.getPassword(); // 해시된 비밀번호
        this.role = user.getRole();
    }

    // JWT용: DTO 기반 (비밀번호 없음)
    public CustomUserDetails(LoginUserDTO dto) {
        this.email = dto.email();
        this.passwordHash = null; // JWT 경로에서는 불필요
        this.role = dto.role();
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.List.of(() -> role.getValue());
    }

    // 나머지 메서드들...
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

