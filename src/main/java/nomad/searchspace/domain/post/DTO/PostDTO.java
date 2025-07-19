package nomad.searchspace.domain.post.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import nomad.searchspace.domain.post.entity.Post;
import nomad.searchspace.domain.post.entity.PostType;
import java.util.List;

@Data
@Builder
public class PostDTO {
    @Schema(description = "공간 이름" ,example = "스타벅스 의정부 신곡점")
    private String title;
    @Schema(description = "공간 설명" ,example = "넓고 깔끔하고 조용해요")
    private String content;
    @Schema(description = "주소", example = "의정부시 능곡로 16-7")
    private String address;

    @Schema(description = "타입", example = "CAFE")
    private PostType type;

    @Schema(description = "위도" , example = "37.7397891880448")
    private double latitude;

    @Schema(description = "경도",example = "127.059614548856")
    private double longitude;

    private String phoneNumber;

    @Schema(description = "영업 시간", example = "수: 10:00-23:59\n" +
            " 목: 10:00-23:59\n" +
            " 금: 10:00-23:59\n" +
            " 토: 10:00-23:59\n" +
            " 일: 영업 정보 없음\n" +
            " 월: 10:00-23:59\n" +
            " 화: 10:00-23:59")
    private String businessHours;

    @Schema(description = "일")
    private String holidays;
    private String url;

    private boolean copyright;
    private boolean approval;
}