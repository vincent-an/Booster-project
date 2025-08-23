package fortuneCookie.booster.domain.borad.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class HomeIntroResponse {

    private Long userId;

    private Long postId;

    private String title;

    private String contentPreview;

    private String profileImageUrl;

    private Boolean isAnonymous;

    private String authorNickname;

    private LocalDateTime createPostTime;

    private int commentCount;

    private int likeCount;

    private Category category;

    // 🔍 Post 엔티티에서 HomePostResponse로 변환
    public static HomeIntroResponse from(Post post) {
        return HomeIntroResponse.builder()
                .userId(post.getIsAnonymous() ? null : post.getUser().getUserId())
                .postId(post.getPostId())
                .title(post.getTitle())
                .contentPreview(createContentPreview(post.getContent()))
                .profileImageUrl(post.getIsAnonymous() ? null :
                        (post.getUser() != null ? post.getUser().getProfileImageUrl() : null))
                .isAnonymous(post.getIsAnonymous())
                .authorNickname(post.getIsAnonymous() ? null :
                        (post.getUser() != null ? post.getUser().getNickname() : null))
                .createPostTime(post.getCreatePostTime())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .category(post.getCategory())
                .build();
    }

    // 내용 1줄로 요약
    private static String createContentPreview(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        String singleLine = content.replaceAll("\\r?\\n", " ");
        if (singleLine.length() > 50) {
            return singleLine.substring(0, 50) + "...";
        }
        return singleLine;
    }
}
