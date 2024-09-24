package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
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

    @Getter
    public static class PointHistoryDetail {
        private final long id;
        private final long userId;
        private final long amount;
        private final TransactionType type;
        private final long updateMillis;

        private PointHistoryDetail(long id, long userId, long amount,
            TransactionType type,
            long updateMillis) {
            this.id = id;
            this.userId = userId;
            this.amount = amount;
            this.type = type;
            this.updateMillis = updateMillis;
        }

        public static PointHistoryDetail of(PointHistory pointHistory) {
            return new PointHistoryDetail(pointHistory.id(), pointHistory.userId(),
                pointHistory.amount(), pointHistory.type(), pointHistory.updateMillis());
        }
    }
}
