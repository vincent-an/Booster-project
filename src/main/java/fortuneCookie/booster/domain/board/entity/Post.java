package fortuneCookie.booster.domain.board.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fortuneCookie.booster.domain.board.entity.enums.Category;
import fortuneCookie.booster.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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

    // 익명 여부
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @JsonIgnore // 혹시 엔티티 직렬화될 때 노출 방지(우리는 DTO로 응답하니 안전빵)
    @Column(name = "anonymous", nullable = true) // DB가 NOT NULL이면 그대로 둬도 됩니다
    private Boolean anonymousMirror = false;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    private List<String> imgURL = new ArrayList<>();

    private String introImgURL;

    @CreationTimestamp
    private LocalDateTime createPostTime;

    // 작성자 (필드명: user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 댓글 목록
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 좋아요 목록
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    // ----- 편의 메서드 -----

    public int getCommentCount() {
        return comments.size();
    }

    public int getLikeCount() {
        return postLikes.size();
    }

    /** 현재 사용자 기준으로 이 게시글을 좋아요 했는지 */
    public boolean isLikedBy(User u) {
        if (u == null) return false;
        for (PostLike like : postLikes) {
            if (like.getUser() != null && like.getUser().equals(u)) {
                return true;
            }
        }
        return false;
    }

    /** 연관관계 설정 편의 메서드 */
    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getPosts() != null && !user.getPosts().contains(this)) {
            user.getPosts().add(this);
        }
    }

    /** 표시용 이름 (선택 사용) */
    @Transient
    public String getDisplayAuthorName() {
        if (Boolean.TRUE.equals(isAnonymous)) return "익명";
        return (user != null) ? user.getNickname() : "탈퇴회원";
    }

    // ----- 생성 메서드 -----

    public static Post of(String title,
                          String content,
                          Category category,
                          Boolean anonymous,
                          User author) {
        Post p = new Post();
        p.setTitle(title);
        p.setContent(content);
        p.setCategory(category);
        p.setIsAnonymous(Boolean.TRUE.equals(anonymous));
        p.setUser(author); // 필드명은 user이므로 setUser 사용
        return p;
    }
    @PrePersist
    @PreUpdate
    private void syncAnonymousMirror() {
        if (this.isAnonymous == null) this.isAnonymous = false;
        // 두 컬럼을 항상 동일하게 맞춰 DB 제약 위반 방지
        this.anonymousMirror = this.isAnonymous;
    }
}
