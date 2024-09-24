package io.hhplus.tdd.point.service;


import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointRepository userPointRepository;

    public void getUserPoint(long id) {
        UserPoint userPoint = userPointRepository.selectById(id)
            .orElseThrow(() -> PointException.NOT_FOUND_USER_POINT);
    }
}
