package nomad.searchspace.domain.member.domain;

import jakarta.persistence.*;
import lombok.*;
import nomad.searchspace.domain.BaseTimeEntity;
import nomad.searchspace.domain.Review.entity.Review;
import nomad.searchspace.domain.like.entity.Likes;
import nomad.searchspace.domain.post.entity.Post;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Setter
    private String password;

    @Setter
    @Column(length = 50)
    private String nickname;

    @Setter
    private Boolean gender;

    @Setter
    @Column(length = 50)
    private String birth;

    @Setter
    @Column(length = 50)
    private String phoneNumber;

    private String role;

    @Setter
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes;

    @Setter
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @Setter
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @Builder
    private Member(String password, String email,  String nickname, Boolean gender, String birth, String phoneNumber, String role) {
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

}