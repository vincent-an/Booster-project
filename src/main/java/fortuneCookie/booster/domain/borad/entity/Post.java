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
@Builder
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

    @ManyToMany(mappedBy = "likedPosts")
    private List<User> likedByUsers = new ArrayList<>();

    public void setUser(User user) {
        this.user = user;
        user.getPosts().add(this);
    }
}
