package fortuneCookie.booster.domain.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDeleteRequest {
    private String email;      // 작성자 검증용 (필수)
}