package io.hhplus.tdd.point.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserPointTest {

    @DisplayName("포인트 차감 - use() 테스트")
    @Nested
    class UseTest {
        @DisplayName("잔액이 부족하면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_PointIsNotEnough() {
            // given
            long amount = 100L;
            UserPoint userPoint = new UserPoint(0L, 50L, System.currentTimeMillis());

            // when, then
            assertThatThrownBy(() -> userPoint.use(amount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.INSUFFICIENT_POINT_BALANCE.getMessage());
        }
    }
}