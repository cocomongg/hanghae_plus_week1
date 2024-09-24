package io.hhplus.tdd.common.exception;

import io.hhplus.tdd.common.error.ErrorResponse;

public interface ApplicationErrorCode {
    ErrorResponse toErrorResponse();
}
