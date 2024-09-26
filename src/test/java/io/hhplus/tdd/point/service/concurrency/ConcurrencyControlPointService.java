package io.hhplus.tdd.point.service.concurrency;

import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.dto.PointDto.PointHistoryDetail;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ConcurrencyControlPointService {

    public UserPointRepository userPointRepository;
    public PointHistoryRepository pointHistoryRepository;
    public PointValidator pointValidator;

    public static final long MAX_AMOUNT = 100_000;

    public ConcurrencyControlPointService(UserPointRepository userPointRepository,
        PointHistoryRepository pointHistoryRepository, PointValidator pointValidator) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.pointValidator = pointValidator;
    }


    public PointDetail getUserPoint(long id) {
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

    public abstract PointDetail charge(long id, long amount);
    public abstract PointDetail use(long id, long amount);
}
