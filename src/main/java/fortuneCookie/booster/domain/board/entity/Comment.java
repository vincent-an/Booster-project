package fortuneCookie.booster.domain.board.entity;

import fortuneCookie.booster.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 익명 여부
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @CreationTimestamp
    private LocalDateTime createCommentTime;

    // 작성자 (필드명: user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 소속된 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** 연관관계 편의 메서드 */
    public void setPost(Post post) {
        this.post = post;
        if (post != null && post.getComments() != null && !post.getComments().contains(this)) {
            post.getComments().add(this);
        }
    }

    /** 표시용 이름 (선택 사용) */
    @Transient
    public String getDisplayAuthorName() {
        if (Boolean.TRUE.equals(isAnonymous)) return "익명";
        return (user != null) ? user.getNickname() : "탈퇴회원";
    }

    /** 서비스에서 쓰는 팩토리 메서드 (4개 인자) */
    public static Comment of(String content,
                             Boolean anonymous,
                             User user,
                             Post post) {
        Comment c = new Comment();
        c.setContent(content);
        c.setIsAnonymous(Boolean.TRUE.equals(anonymous));
        c.setUser(user);
        c.setPost(post);
        return c;
    }
}
