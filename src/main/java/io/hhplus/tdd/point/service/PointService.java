package io.hhplus.tdd.point.service;


import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.dto.PointDto.PointHistoryDetail;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

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
}
