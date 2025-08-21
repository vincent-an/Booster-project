package fortuneCookie.booster.domain.board.dto;

import fortuneCookie.booster.domain.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private boolean anonymous;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime createdAt;
    private String authorName;

    public static CommentResponse of(Comment c) {
        return CommentResponse.builder()
                .id(c.getCommentId())
                .content(c.getContent())
                .anonymous(Boolean.TRUE.equals(c.getIsAnonymous()))
                .authorId(c.getUser() != null ? c.getUser().getUserId() : null)
                .authorNickname(c.getUser() != null ? c.getUser().getNickname() : null)
                .createdAt(c.getCreateCommentTime())
                .authorName(Boolean.TRUE.equals(c.getIsAnonymous()) ? "익명"
                        : (c.getUser() != null ? c.getUser().getNickname() : "탈퇴회원"))
                .build();
    }
}
