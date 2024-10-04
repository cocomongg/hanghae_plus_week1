package io.hhplus.tdd.point.model;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

    public static PointHistory createChargeHistory(long userId, long amount, long updateMillis) {
        // log idx (여기서는 Id)는 Auto Increment때문에 id에 0L삽입
        return new PointHistory(0L, userId, amount, TransactionType.CHARGE, updateMillis);
    }

    public static PointHistory createUseHistory(long userId, long amount, long updateMillis) {
        // log idx (여기서는 Id)는 Auto Increment때문에 id에 0L삽입
        return new PointHistory(0L, userId, amount, TransactionType.USE, updateMillis);
    }
}
