package fortuneCookie.booster.domain.user.dto;

import fortuneCookie.booster.domain.user.entity.enums.Department;
import fortuneCookie.booster.domain.user.entity.enums.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JoinRequest {
    private String email;
    private String password;
    private String nickname;
    private Gender gender;
    private int admissionYear;
    private Department department;
}
