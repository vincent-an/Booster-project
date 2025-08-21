package fortuneCookie.booster.domain.board.dto;

import fortuneCookie.booster.domain.board.entity.Post;
import fortuneCookie.booster.domain.board.entity.enums.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private Category category;
    private boolean anonymous;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean likedByMe;
    private List<CommentResponse> comments;
    private String authorName;

    public static PostResponse from(Post p, boolean likedByMe, List<CommentResponse> comments) {
        return PostResponse.builder()
                .id(p.getPostId())
                .title(p.getTitle())
                .content(p.getContent())
                .category(p.getCategory())
                .anonymous(Boolean.TRUE.equals(p.getIsAnonymous()))
                .authorId(p.getUser() != null ? p.getUser().getUserId() : null)
                .authorNickname(p.getUser() != null ? p.getUser().getNickname() : null)
                .createdAt(p.getCreatePostTime())
                .likeCount(p.getPostLikes() != null ? p.getPostLikes().size() : 0)
                .likedByMe(likedByMe)
                .comments(comments)
                .authorName(Boolean.TRUE.equals(p.getIsAnonymous()) ? "익명"
                        : (p.getUser() != null ? p.getUser().getNickname() : "탈퇴회원"))
                .build();
    }
}
