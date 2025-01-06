package nomad.searchspace.domain.member.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import nomad.searchspace.domain.BaseTimeEntity;
import nomad.searchspace.domain.like.entity.Likes;
import nomad.searchspace.domain.scrap.domain.Scrap;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "이메일은 필수입니다.")
    @Column(unique = true, nullable = false)
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

    private String role;

    @Setter
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes;

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scrap> scraps;

    @Builder
    private Member(String password, String email,  String nickname, Boolean gender, String birth, String role) {
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        this.birth = birth;
        this.role = role;
    }

}