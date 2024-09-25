package io.hhplus.tdd.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private final int status;
    private final T data;

    public ApiResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public static ApiResponse<?> defaultOk() {
        return new ApiResponse<>(HttpStatus.OK.value(), null);
    }

    public static <T> ApiResponse<T> defaultOk(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), data);
    }
}
