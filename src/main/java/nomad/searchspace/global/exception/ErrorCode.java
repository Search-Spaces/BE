package nomad.searchspace.global.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "공간을 찾을 수 없습니다"),
    SEARCHRESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과를 찾을 수 없습니다."),
    MEMBER_NOW_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String msg;

}
