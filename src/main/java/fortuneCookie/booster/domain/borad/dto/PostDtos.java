package fortuneCookie.booster.domain.borad.dto;

import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.borad.entity.Comment;
import fortuneCookie.booster.domain.borad.entity.enums.Category;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PostDtos {

    @Getter @Setter
    public static class CreateRequest {
        private String title;
        private String content;
        private Category category;
        private Boolean anonymous = false;
        private String email; // 인증 연동 전 임시
    }

    @Getter @Builder
    public static class Response {
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

        public static Response from(Post p, boolean likedByMe, List<CommentResponse> comments) {
            return Response.builder()
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
                    .build();
        }
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String title;
        private String content;
        private Category category;
        private Boolean anonymous;
    }

    @Getter @Builder
    public static class PageResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class CommentCreateRequest {
        private String content;
        private Boolean anonymous = false;
        private String email;
    }

    @Getter @Builder
    public static class CommentResponse {
        private Long id;
        private String content;
        private boolean anonymous;
        private Long authorId;
        private String authorNickname;
        private LocalDateTime createdAt;

        public static CommentResponse of(Comment c) {
            return CommentResponse.builder()
                    .id(c.getCommentId())
                    .content(c.getContent())
                    .anonymous(Boolean.TRUE.equals(c.getIsAnonymous()))
                    .authorId(c.getUser() != null ? c.getUser().getUserId() : null)
                    .authorNickname(c.getUser() != null ? c.getUser().getNickname() : null)
                    .createdAt(c.getCreateCommentTime())
                    .build();
        }
    }
}
