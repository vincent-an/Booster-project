package fortuneCookie.booster.domain.borad.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fortuneCookie.booster.domain.borad.entity.Comment;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentResponse {

    private Long commentUserId; // 방금 추가

    private Long commentId;

    private String content;

    private Boolean isAnonymous;

    private String authorNickname; // 작성자 닉네임 (익명이면 null)

    private String formattedDate;

    private ZonedDateTime createCommentTime;

    private Boolean isAuthor; // 글쓴이 여부 (true: 글쓴이, false: 일반 댓글)

    public static CommentResponse from(Comment comment, User currentUser, Post post) {
        // 글쓴이 여부 확인
        boolean isPostAuthor = comment.getUser().getUserId().equals(post.getUser().getUserId());

        // 닉네임 처리
        String nickname = null;
        if (!comment.getIsAnonymous()) {
            nickname = comment.getUser().getNickname();
            // 글쓴이면 닉네임 뒤에 (글쓴이) 추가
            if (isPostAuthor) {
                nickname += "(글쓴이)";
            }
        } else {
            // 익명이지만 글쓴이면 "익명(글쓴이)"
            if (isPostAuthor) {
                nickname = "익명(글쓴이)";
            } else {
                nickname = "익명";
            }
        }

        return CommentResponse.builder()
                .commentUserId(comment.getUser().getUserId())
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .isAnonymous(comment.getIsAnonymous())
                .authorNickname(nickname)
                .formattedDate(formatDate(comment.getCreateCommentTime().toLocalDateTime()))
                .createCommentTime(comment.getCreateCommentTime())
                .isAuthor(isPostAuthor)
                .build();
    }

    // 날짜 포맷 (MM/DD HH:mm)
    private static String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        return dateTime.format(formatter);
    }
}
