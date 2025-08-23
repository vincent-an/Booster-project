package fortuneCookie.booster.domain.user.dto.request;

import fortuneCookie.booster.domain.user.entity.enums.Department;
import fortuneCookie.booster.domain.user.entity.enums.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {
    private String nickname;
    private Gender gender;
    private int admissionYear;
    private Department department;
}
