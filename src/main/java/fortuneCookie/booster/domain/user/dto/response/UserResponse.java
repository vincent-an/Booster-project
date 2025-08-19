package fortuneCookie.booster.domain.user.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.entity.enums.Department;
import fortuneCookie.booster.domain.user.entity.enums.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class) //user_id를 -> userId처럼 변경해주는 코드
public class UserResponse {

    private Long userId;
    private String nickname;
    private Gender gender;
    private int admissionYear;
    private Department department;
    private String message;

    public static UserResponse from(User user, String message) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .admissionYear(user.getAdmissionYear())
                .department(user.getDepartment())
                .message(message)
                .build();
    }
}
