package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.common.exception.ApplicationException;

public class PointException extends ApplicationException {

    public static final PointException NOT_FOUND_USER_POINT =
        new PointException(PointErrorCode.NOT_FOUND_USER_POINT);

    public PointException(PointErrorCode pointErrorCode) {
        super(pointErrorCode);
    }
}
