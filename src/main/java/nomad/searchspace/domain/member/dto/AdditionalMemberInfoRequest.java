package nomad.searchspace.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Schema(title = "회원가입 요청 DTO")
@Getter
@NoArgsConstructor
public class AdditionalMemberInfoRequest {


    @Schema(description = "닉네임 입력", example = "김해피")
    @Length(min = 2, max = 10)
    private String nickname;

    @Schema(description = "성별 입력 남자 : false, 여자 : true", example = "false")
    private Boolean gender;

    @Schema(description = "생일 입력", example = "2001-01-01")
    @NotNull
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
            message = "생일은 yyyy-MM-dd 형식이어야 합니다.")
    private String birth;

    @Schema(description = "핸드폰 번호 입력", example = "111-1111-1111")
    @NotNull
    private String phoneNumber;

    private AdditionalMemberInfoRequest(String nickname, Boolean gender, String birth, String phoneNumber) {
        this.nickname = nickname;
        this.gender = gender;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
    }

}
