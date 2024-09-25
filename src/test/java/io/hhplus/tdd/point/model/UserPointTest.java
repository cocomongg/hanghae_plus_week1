package io.hhplus.tdd.point.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserPointTest {

    @DisplayName("포인트 충전 - charge() 테스트")
    @Nested
    class ChargeTest {
        @DisplayName("포인트를 충전한 만큼 더하여 UserPoint를 반환한다.")
        @Test
        void should_PlusPointAndReturn_When_Charge() {
            // given
            long amountToCharge = 100L;
            long balanceAmount = 50L;
            UserPoint userPoint = new UserPoint(0L, balanceAmount, System.currentTimeMillis());

            // when
            UserPoint chargedUserPoint = userPoint.charge(amountToCharge);

            // then
            assertThat(chargedUserPoint.point()).isEqualTo(amountToCharge + balanceAmount);
            assertThat(chargedUserPoint.id()).isEqualTo(0L);
        }
    }

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

        @DisplayName("포인트를 사용하면 사용한 만큼 차감된 UserPoint를 반환한다.")
        @Test
        void should_MinusPointAndReturn_When_Use() {
            // given
            long amountToUse = 50L;
            long existAmount = 100L;
            UserPoint userPoint = new UserPoint(0L, existAmount, System.currentTimeMillis());

            // when
            UserPoint usedUserPoint = userPoint.use(amountToUse);

            // then
            assertThat(usedUserPoint.id())
                .isEqualTo(userPoint.id());
            assertThat(usedUserPoint.point())
                .isEqualTo(existAmount - amountToUse);
        }
    }
}