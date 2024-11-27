package nomad.searchspace.domain.post.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    @Column(nullable = false, length = 50)
    private String title;
    @Column(nullable = false, length = 1000)
    private String content;
    @Column(nullable = false, length = 50)
    private String address;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type; //영업종류
    @Column(nullable = false)
    private double latitude; //위도
    @Column(nullable = false)
    private double longitude; //경도
    @Column(length = 50)
    private String phoneNumber;
    @Column(length = 250)
    private String businessHours; //영업시간
    @Column(length = 250)
    private String holidays; //휴무일
    @Column(length = 250)
    private String url;
    @Column
    private boolean copyright = false; //저작권 여부
    @Column
    private boolean approval =false; //승인여부
    @Column(length = 50)
    private int likeCount = 0; //좋아요 수

    @OneToMany(mappedBy = "post")
    private List<PostImage> images;

}
