package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.common.error.ErrorResponse;
import io.hhplus.tdd.common.exception.ApplicationErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointErrorCode implements ApplicationErrorCode {

    INVALID_POINT_AMOUNT("400_1", "Point는 음수이면 안됩니다."),
    NOT_FOUND_USER_POINT("404_1", "UserPoint를 찾을 수 없습니다.");

    private final String code;
    private final String message;

    @Override
    public ErrorResponse toErrorResponse() {
        return new ErrorResponse(this.code, this.message);
    }
}
