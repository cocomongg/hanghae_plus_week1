package io.hhplus.tdd.point.model;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

    public static PointHistory createChargeHistory(long userId, long amount, long updateMillis) {
        // log idx (여기서는 Id)는 Auto_increment라고 가정해서 id에 0L삽입
        return new PointHistory(0L, userId, amount, TransactionType.CHARGE, updateMillis);
    }
}
