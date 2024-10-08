package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.common.exception.ApplicationException;

public class PointException extends ApplicationException {

    public static final PointException NOT_FOUND_USER_POINT =
        new PointException(PointErrorCode.NOT_FOUND_USER_POINT);

    public static final PointException INSUFFICIENT_POINT_BALANCE =
        new PointException(PointErrorCode.INSUFFICIENT_POINT_BALANCE);

    public static final PointException EXCEED_POINT_BALANCE =
        new PointException(PointErrorCode.EXCEED_POINT_BALANCE);

    public static final PointException INVALID_POINT_AMOUNT =
        new PointException(PointErrorCode.INVALID_POINT_AMOUNT);

    public PointException(PointErrorCode pointErrorCode) {
        super(pointErrorCode);
    }
}
