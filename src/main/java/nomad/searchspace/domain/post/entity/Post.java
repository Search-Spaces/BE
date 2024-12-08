package nomad.searchspace.domain.post.entity;


import jakarta.persistence.*;
import lombok.*;
import nomad.searchspace.domain.like.entity.Likes;

import java.util.List;

@Entity
@Getter
@Setter
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images;

    @Builder
    public Post(String title, String content, String address, PostType type, double latitude,
                double longitude, String phoneNumber, String businessHours, String holidays,
                String url, boolean copyright, boolean approval) {
        this.title = title;
        this.content = content;
        this.address = address;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.businessHours = businessHours;
        this.holidays = holidays;
        this.url = url;
        this.copyright = copyright;
        this.approval = approval;
    }
}
