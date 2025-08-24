package fortuneCookie.booster.domain.borad.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostIntroResponse {
    private Long userId;

    private Long postId;

    private String title;

    private String contentPreview; // 내용 축약

    private String introImgURL;

    private int commentCount;

    private int likeCount;

    private Category category;

    private ZonedDateTime createPostTime;

    public static PostIntroResponse from(Post post) {
        return PostIntroResponse.builder()
                .userId(post.getIsAnonymous() ? null : post.getUser().getUserId())  // 익명이면 null
                .postId(post.getPostId())
                .title(post.getTitle())
                .contentPreview(createContentPreview(post.getContent()))  // 내용 1줄로 축약
                .introImgURL(post.getIntroImgURL())  // 없으면 null
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .category(post.getCategory())
                .createPostTime(post.getCreatePostTime())
                .build();
    }

    // 내용 축약 메서드
    private static String createContentPreview(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        // 개행 문자 제거하고 공백으로 대체
        String singleLine = content.replaceAll("\\r?\\n", " ");

        // 50자로 자르고 뒤에 "..." 추가
        if (singleLine.length() > 30) {
            return singleLine.substring(0, 30) + "...";
        }

        return singleLine;
    }
}
