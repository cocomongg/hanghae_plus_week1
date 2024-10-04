package io.hhplus.tdd.point.service.concurrency;

import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;

public class PointServiceWithSynchronized extends ConcurrencyControlPointService {

    public PointServiceWithSynchronized(UserPointRepository userPointRepository,
        PointHistoryRepository pointHistoryRepository, PointValidator pointValidator) {
        super(userPointRepository, pointHistoryRepository, pointValidator);
    }

    @Override
    public PointDetail charge(long id, long amount) {
        pointValidator.checkAmount(amount);

        synchronized (this) {
            UserPoint userPoint = userPointRepository.selectById(id)
                .orElse(UserPoint.empty(id));

            UserPoint savedUserPoint =
                userPointRepository.insertOrUpdate(userPoint.charge(amount, MAX_AMOUNT));

            PointHistory chargeHistory =
                PointHistory.createChargeHistory(id, amount, System.currentTimeMillis());
            pointHistoryRepository.insert(chargeHistory);

            return PointDetail.of(savedUserPoint);
        }
    }

    @Override
    public PointDetail use(long id, long amount) {
        pointValidator.checkAmount(amount);

        synchronized (this) {
            UserPoint userPoint = userPointRepository.selectById(id)
                .orElseThrow(() -> PointException.NOT_FOUND_USER_POINT);

            UserPoint upatedUserPoint = userPointRepository.insertOrUpdate(userPoint.use(amount));

            PointHistory chargeHistory =
                PointHistory.createUseHistory(id, amount, System.currentTimeMillis());
            pointHistoryRepository.insert(chargeHistory);

            return PointDetail.of(upatedUserPoint);
        }
    }
}
