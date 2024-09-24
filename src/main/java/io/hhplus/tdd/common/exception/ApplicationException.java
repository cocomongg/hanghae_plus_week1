package io.hhplus.tdd.common.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {
    private final ApplicationErrorCode errorCode;

    public ApplicationException(ApplicationErrorCode errorCode) {
        super(errorCode.toErrorResponse().message());
        this.errorCode = errorCode;
    }
}