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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private final PointValidator pointValidator;

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

        UserPoint userPoint = userPointRepository.selectById(id)
            .orElse(UserPoint.empty(id));

        UserPoint chargedUserPoint = userPoint.charge(amount);
        UserPoint savedUserPoint = userPointRepository.insertOrUpdate(chargedUserPoint);

        PointHistory chargeHistory = PointHistory.createChargeHistory(savedUserPoint.id(),
            savedUserPoint.point(), savedUserPoint.updateMillis());
        pointHistoryRepository.insert(chargeHistory);

        return PointDetail.of(savedUserPoint);
    }
}