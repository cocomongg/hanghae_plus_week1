package io.hhplus.tdd.point.validator;

import io.hhplus.tdd.point.exception.PointException;
import org.springframework.stereotype.Component;

@Component // 추후 추가될 외부 의존성을 고려하여 bean으로 등록
public class PointValidator {

    public void checkAmount(long amount) {
        if(amount <= 0) {
            throw PointException.INVALID_POINT_AMOUNT;
        }
    }
}
