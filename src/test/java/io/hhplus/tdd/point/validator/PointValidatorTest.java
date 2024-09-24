package io.hhplus.tdd.point.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PointValidatorTest {

    private final PointValidator pointValidator = new PointValidator();

    @DisplayName("amount 검사 테스트")
    @Nested
    class CheckAmountTest {
        @DisplayName("amount가 음수면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_AmountIsNegative () {
            // given
            long negativeAmount = -100L;

            // when, then
            assertThatThrownBy(() -> pointValidator.checkAmount(negativeAmount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.INVALID_POINT_AMOUNT.getMessage());
        }
    }
}