package io.hhplus.tdd.point.service.concurrency;

import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PointServiceWithSelectiveLock extends ConcurrencyControlPointService {

    public PointServiceWithSelectiveLock(
        UserPointRepository userPointRepository,
        PointHistoryRepository pointHistoryRepository,
        PointValidator pointValidator) {
        super(userPointRepository, pointHistoryRepository, pointValidator);
    }

    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public PointDetail charge(long id, long amount) {
        pointValidator.checkAmount(amount);

        ReentrantLock lock = lockMap.computeIfAbsent(id, key -> new ReentrantLock());
        lock.lock();
        try {
            UserPoint userPoint = userPointRepository.selectById(id)
                .orElse(UserPoint.empty(id));

            UserPoint savedUserPoint =
                userPointRepository.insertOrUpdate(userPoint.charge(amount, MAX_AMOUNT));

            PointHistory chargeHistory =
                PointHistory.createChargeHistory(id, amount, System.currentTimeMillis());
            pointHistoryRepository.insert(chargeHistory);

            return PointDetail.of(savedUserPoint);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public PointDetail use(long id, long amount) {
        pointValidator.checkAmount(amount);

        ReentrantLock lock = lockMap.computeIfAbsent(id, key -> new ReentrantLock());
        lock.lock();
        try {
            UserPoint userPoint = userPointRepository.selectById(id)
                .orElseThrow(() -> PointException.NOT_FOUND_USER_POINT);

            UserPoint upatedUserPoint = userPointRepository.insertOrUpdate(userPoint.use(amount));

            PointHistory chargeHistory =
                PointHistory.createUseHistory(id, amount, System.currentTimeMillis());
            pointHistoryRepository.insert(chargeHistory);

            return PointDetail.of(upatedUserPoint);
        } finally {
            lock.unlock();
        }
    }
}
