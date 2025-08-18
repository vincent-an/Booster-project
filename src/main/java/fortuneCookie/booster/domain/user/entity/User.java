package fortuneCookie.booster.domain.user.entity;

import fortuneCookie.booster.domain.borad.entity.Comment;
import fortuneCookie.booster.domain.borad.entity.PostLike;
import fortuneCookie.booster.domain.borad.entity.Post;
import fortuneCookie.booster.domain.user.entity.enums.Department;
import fortuneCookie.booster.domain.user.entity.enums.Gender;
import fortuneCookie.booster.domain.user.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;  // 학교 이메일 (로그인 ID)

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "admission_year")
    private int admissionYear;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Department department;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 연관관계 매핑
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    // 편의 메서드 (사용자가 좋아요 누른 게시글 조회)
    public List<Post> getLikedPosts() {
        return postLikes.stream()
                .map(PostLike::getPost)
                .collect(Collectors.toList());
    }

    @Builder
    private User(
            String email,
            String password,
            String nickname,
            int admissionYear,
            Gender gender,
            Department department,
            Role role
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.admissionYear = admissionYear;
        this.gender = gender;
        this.department = department;
        this.role = role;
    }
}
