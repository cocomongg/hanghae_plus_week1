package io.hhplus.tdd.point.service;


import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.dto.PointDto.PointHistoryDetail;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private final PointValidator pointValidator;

    private final SelectiveLockFactory lockFactory;

    public PointDetail getUserPoint(long id) throws PointException {
        UserPoint userPoint = userPointRepository.selectById(id)
            .orElseThrow(() -> PointException.NOT_FOUND_USER_POINT);

        return PointDetail.of(userPoint);
    }

    public List<PointHistoryDetail> getUserPointHistories(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId)
            .stream()
            .map(PointHistoryDetail::of)
            .collect(Collectors.toList());
    }

    public PointDetail charge(long id, long amount) {
        pointValidator.checkAmount(amount);

        ReentrantLock lock = lockFactory.getLock(id);
        lock.lock();
        try {
            UserPoint userPoint = userPointRepository.selectById(id)
                .orElse(UserPoint.empty(id));

            UserPoint savedUserPoint = userPointRepository.insertOrUpdate(userPoint.charge(amount));

            PointHistory chargeHistory =
                PointHistory.createChargeHistory(id, amount, System.currentTimeMillis());
            pointHistoryRepository.insert(chargeHistory);

            return PointDetail.of(savedUserPoint);
        } finally {
            lock.unlock();
        }
    }

    public PointDetail use(long id, long amount) {
        pointValidator.checkAmount(amount);

        ReentrantLock lock = lockFactory.getLock(id);
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