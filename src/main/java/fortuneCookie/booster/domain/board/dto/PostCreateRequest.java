package fortuneCookie.booster.domain.board.dto;

import fortuneCookie.booster.domain.board.entity.enums.Category;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    private String title;
    private String content;
    private Category category;
    private Boolean anonymous = false;
    private String email; // 인증 연동 전 임시
}
