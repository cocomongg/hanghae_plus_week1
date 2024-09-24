package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.model.UserPoint;
import lombok.Getter;

public class PointDto {

    @Getter
    public static class PointDetail {
        private final long id;
        private final long pointAmount;
        private final long updateMillis;

        private PointDetail(long id, long pointAmount, long updateMillis) {
            this.id = id;
            this.pointAmount = pointAmount;
            this.updateMillis = updateMillis;
        }

        public static PointDetail of(UserPoint userPoint) {
            return new PointDetail(userPoint.id(), userPoint.point(), userPoint.updateMillis());
        }
    }
}
