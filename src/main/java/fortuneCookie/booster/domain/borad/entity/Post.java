package fortuneCookie.booster.domain.borad.entity;

import fortuneCookie.booster.domain.borad.entity.enums.Category;
import fortuneCookie.booster.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isAnonymous = false;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    private List<String> imgURL = new ArrayList<>();

    private String introImgURL;

    @CreationTimestamp
    private LocalDateTime createPostTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    // 댓글 수
    public int getCommentCount() {
        return comments.size();
    }

    // 좋아요 수
    public int getLikeCount() {
        return postLikes.size();
    }

    // 현재 사용자의 특정 게시글 좋아요 여부
    public boolean isLikedBy(User user) {
        return postLikes.stream()
                .anyMatch(postLike -> postLike.getUser().equals(user));
    }

    public void setUser(User user) {
        this.user = user;
        user.getPosts().add(this);
    }
    public static Post of(String title,
                          String content,
                          Category category,
                          Boolean anonymous,
                          User author) {
        Post p = new Post();                 // 클래스 내부라 protected 생성자 접근 가능
        p.setTitle(title);
        p.setContent(content);
        p.setCategory(category);
        p.setIsAnonymous(Boolean.TRUE.equals(anonymous));
        p.setUser(author);                   // 연관관계 편의 메서드 있으면 그대로 사용
        return p;
    }
}