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
    private String title;  // 게시글 제목 (필수)

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

    @Builder
    public Post(String title, String content, Boolean isAnonymous, Category category,
                List<String> imgURL, String introImgURL, User user) {
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous != null ? isAnonymous : false;  // null이면 기본값 false
        this.category = category;
        this.imgURL = imgURL != null ? imgURL : new ArrayList<>();     // null이면 빈 리스트
        this.introImgURL = introImgURL;
        this.user = user;
    }
}
