package fortuneCookie.booster.domain.borad.entity;
import fortuneCookie.booster.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    private String content;

    private Boolean isAnonymous = true;

    @CreationTimestamp
    private LocalDateTime createCommentTime;

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public Comment(String content, Boolean isAnonymous, User user, Post post) {
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.user = user;
        this.post = post;
    }

    public void setUser(User user) {
        this.user = user;
        user.getComments().add(this);
    }

    public void setPost(Post post) {
        this.post = post;
        post.getComments().add(this);
    }
    public static Comment of(String content,
                             Boolean anonymous,
                             User user,
                             Post post) {
        Comment c = new Comment();           // 클래스 내부라 protected 생성자 접근 가능
        c.setContent(content);
        c.setIsAnonymous(Boolean.TRUE.equals(anonymous));
        c.setUser(user);
        c.setPost(post);
        return c;
    }
}