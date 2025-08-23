package fortuneCookie.booster.domain.borad.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import fortuneCookie.booster.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostResponse {
    private Long authorId;
    private Long postId;
    private String title;
    private String content;
    private Category category;
    private Boolean isAnonymous;
    private String authorNickname;
    private String introImgURL;
    private List<String> imgURL = new ArrayList<>();
    private LocalDateTime createPostTime;
    private int commentCount;
    private int likeCount;
    // 현재 사용자의 좋아요 여부
    private boolean isLikedByCurrentUser;
    private String message;

    // Post -> PostResponse로 변환하는 정적 메서드
    public static PostResponse from(Post post, User currentUser, String message) {
        return PostResponse.builder()
                .authorId(post.getIsAnonymous() ? null : post.getUser().getUserId()) // 익명이면 null
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .isAnonymous(post.getIsAnonymous())
                .authorNickname(post.getIsAnonymous() ? null : post.getUser().getNickname())  // 익명이면 null
                .introImgURL(post.getIntroImgURL())
                .imgURL(post.getImgURL())
                .createPostTime(post.getCreatePostTime())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .isLikedByCurrentUser(post.isLikedBy(currentUser))  // 현재 사용자의 좋아요 여부
                .message(message)
                .build();
    }
}
