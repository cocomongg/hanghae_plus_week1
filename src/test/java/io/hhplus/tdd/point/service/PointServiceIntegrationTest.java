package io.hhplus.tdd.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryInMemoryRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointInMemoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private UserPointRepository userPointRepository;
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    @Autowired
    private PointValidator pointValidator;
    @Autowired
    private PointService pointService;

    @AfterEach
    public void tearDown() {
        if(userPointRepository instanceof UserPointInMemoryRepository) {
            Object userPointTable = ReflectionTestUtils.getField(userPointRepository, "userPointTable");
            if(!ObjectUtils.isEmpty(userPointTable)) {
                ReflectionTestUtils.setField(userPointTable, "table", new HashMap<>());
            }
        }

        if(pointHistoryRepository instanceof PointHistoryInMemoryRepository) {
            Object pointHistoryTable = ReflectionTestUtils.getField(pointHistoryRepository, "pointHistoryTable");
            if(!ObjectUtils.isEmpty(pointHistoryTable)) {
                ReflectionTestUtils.setField(pointHistoryTable, "table", new ArrayList<>());
            }
        }
    }

    @DisplayName("포인트 조회 통합 테스트 - getUserPoint()")
    @Nested
    class GetUserPointIntegrationTest {
        @DisabledIf("isUserPointRepositoryUseTable") // UserPointTable을 사용할때만 Disable처리되도록
        @DisplayName("id에 해당하는 UserPoint가 존재하지 않으면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_UserPointNotFound() {
            // given
            long notExistId = -1L;

            // when, then
            assertThatThrownBy(() -> pointService.getUserPoint(notExistId))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.NOT_FOUND_USER_POINT.getMessage());
        }

        @DisplayName("존재하는 id에 해당하는 UserPoint를 PointDetail Dto로 변환하여 반환한다.")
        @Test
        void should_ReturnPointDetail_When_UserPointFound() {
            // given
            long existId = 0L;
            UserPoint userPoint =
                new UserPoint(existId, 100L, System.currentTimeMillis());
            userPointRepository.insertOrUpdate(userPoint);

            // when
            PointDetail result = pointService.getUserPoint(existId);

            // then
            assertThat(result.getId()).isEqualTo(existId);
            assertThat(result.getPointAmount()).isEqualTo(userPoint.point());
        }

         boolean isUserPointRepositoryUseTable() {
            return userPointRepository instanceof UserPointInMemoryRepository;
        }
    }
}
