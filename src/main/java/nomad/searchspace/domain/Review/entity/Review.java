package nomad.searchspace.domain.Review.entity;

import jakarta.persistence.*;
import lombok.*;
import nomad.searchspace.domain.member.domain.Member;
import nomad.searchspace.domain.post.entity.Post;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "reviewContentType", joinColumns = @JoinColumn(name = "reviewId"))
    @Enumerated(EnumType.STRING)
    private List<ContentType> contentTypesList;

    @Column(length = 300)
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private Member member;


    @Builder
    public Review(List<ContentType> contentTypeList, Post post, Member member, String content) {
        this.contentTypesList = contentTypeList;
        this.post = post;
        this.member = member;
        this.content = content;
    }
}
