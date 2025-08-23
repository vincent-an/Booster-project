package fortuneCookie.booster.domain.user.dto;

import fortuneCookie.booster.domain.user.entity.enums.Department;
import fortuneCookie.booster.domain.user.entity.enums.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDTO {
    String nickname;
    Gender gender;
    int admissionYear;
    Department department;
    private String profileImageUrl;
}
