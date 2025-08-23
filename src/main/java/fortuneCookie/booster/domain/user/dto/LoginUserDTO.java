package fortuneCookie.booster.domain.user.dto;

import fortuneCookie.booster.domain.user.entity.enums.Role;

public record LoginUserDTO(String email, Role role) {
    public static LoginUserDTO of(String email, String roleValue) {
        return new LoginUserDTO(email, Role.fromValue(roleValue));
    }
}

