package fortuneCookie.booster.domain.board.dto;

import fortuneCookie.booster.domain.board.entity.enums.Category;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    private String title;
    private String content;
    private Category category;
    private Boolean anonymous;
}
