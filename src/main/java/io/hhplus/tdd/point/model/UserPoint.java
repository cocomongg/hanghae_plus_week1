package io.hhplus.tdd.point.model;

import io.hhplus.tdd.point.exception.PointException;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        return new UserPoint(id, point + amount, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if(point < amount) {
            throw PointException.INSUFFICIENT_POINT_BALANCE;
        }

        return new UserPoint(id, point - amount, System.currentTimeMillis());
    }
}
