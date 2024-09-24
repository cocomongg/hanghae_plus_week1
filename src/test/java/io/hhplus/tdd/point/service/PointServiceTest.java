package io.hhplus.tdd.point.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.UserPointRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

    @InjectMocks
    private PointService pointService;

    @DisplayName("포인트 조회 - getUserPoint() 테스트")
    @Nested
    class GetUserPoint {
        @DisplayName("id에 해당하는 UserPoint가 존재하지 않으면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_NotExistById() {
            // given
            long notExistId = -1L;

            // when
            when(userPointRepository.selectById(notExistId))
                .thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> pointService.getUserPoint(notExistId))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.NOT_FOUND_USER_POINT.getMessage());
        }
    }
}