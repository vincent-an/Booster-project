package fortuneCookie.booster.domain.board.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateRequest {
    private String content;
    private Boolean anonymous = false;
    private String email;
}
