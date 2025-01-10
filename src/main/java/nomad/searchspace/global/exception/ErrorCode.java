package nomad.searchspace.global.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "공간을 찾을 수 없습니다"),
    SEARCHRESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다"),
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "유효하지 않은 주소입니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출 중 오류가 발생했습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    LIKE_ALREADY_CANCEL(HttpStatus.NOT_FOUND, "좋아요가 이미 취소되었습니다."),
    LIKE_ALREADY_ADD(HttpStatus.NOT_FOUND, "좋아요를 이미 눌렀습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 access token 입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 refresh token 입니다"),
    ACCESS_TOKEN_ALREADY_EXPIRED(HttpStatus.UNAUTHORIZED, "토콘이 이미 만료되었습니다."),

    REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 서버와의 연결에 실패했습니다."),
    REDIS_COMMAND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 명령어 처리 중 오류가 발생했습니다."),
    REDIS_DATA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis에서 처리된 데이터에 문제가 있습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String msg;

}
