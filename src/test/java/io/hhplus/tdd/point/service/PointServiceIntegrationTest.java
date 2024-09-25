package io.hhplus.tdd.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.dto.PointDto.PointHistoryDetail;
import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryInMemoryRepository;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointInMemoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                ReflectionTestUtils.setField(pointHistoryTable, "cursor", 1);
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

    @DisplayName("포인트 내역 조회 통합 테스트 - getPointHistories()")
    @Nested
    class GetPointHistoriesIntegrationTest {
        @DisplayName("userId에 해당하는 내역이 없으면 빈 리스트를 반환한다.")
        @Test
        void should_ReturnEmptyList_When_NotExist() {
            // given
            long notExistsUserId = -1L;

            // when
            List<PointHistoryDetail> results = pointService.getUserPointHistories(notExistsUserId);

            // then
            assertThat(results).isEmpty();
        }

        @DisplayName("userId에 해당하는 PointHistory dto(PointHistoryDetail)목록을 반환한다.")
        @Test
        void should_ReturnHistoryDetailList_When_Exist() {
            // given
            long existUserId = 0L;

            List<PointHistory> expectedPointHistories = List.of(
                new PointHistory(1L, existUserId, 100L, TransactionType.CHARGE,
                    System.currentTimeMillis()),
                new PointHistory(2L, existUserId, 50L, TransactionType.USE,
                    System.currentTimeMillis())
            );

            pointHistoryRepository.insert(expectedPointHistories.get(0));
            pointHistoryRepository.insert(expectedPointHistories.get(1));

            // when
            List<PointHistoryDetail> results = pointService.getUserPointHistories(existUserId);

            // then
            assertThat(results)
                .hasSize(2)
                .extracting(
                    PointHistoryDetail::getId,
                    PointHistoryDetail::getUserId,
                    PointHistoryDetail::getAmount,
                    PointHistoryDetail::getType,
                    PointHistoryDetail::getUpdateMillis)
                .containsExactlyInAnyOrder(
                    tuple(
                        expectedPointHistories.get(0).id(),
                        expectedPointHistories.get(0).userId(),
                        expectedPointHistories.get(0).amount(),
                        expectedPointHistories.get(0).type(),
                        expectedPointHistories.get(0).updateMillis()
                    ),
                    tuple(
                        expectedPointHistories.get(1).id(),
                        expectedPointHistories.get(1).userId(),
                        expectedPointHistories.get(1).amount(),
                        expectedPointHistories.get(1).type(),
                        expectedPointHistories.get(1).updateMillis()
                    )
                );
        }
    }

    @DisplayName("포인트 충전 통합 테스트 - charge()")
    @Nested
    class ChargeIntegrationTest {

        @DisplayName("포인트 충전 금액이 음수라면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_AmountIsNegative () {
            // given
            long negativeAmount = -100L;

            // when, then
            assertThatThrownBy(() -> pointService.charge(0L, negativeAmount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.INVALID_POINT_AMOUNT.getMessage());
        }

        @DisplayName("포인트 충전 금액이 0이라면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_AmountIsZero () {
            // given
            long zeroAmount = 0L;

            // when, then
            assertThatThrownBy(() -> pointService.charge(0L, zeroAmount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.INVALID_POINT_AMOUNT.getMessage());
        }
        
        @DisplayName("포인트 충전을 하면 충전한 만큼 UserPoint가 업데이트되고, PointHistory에 내역을 저장한다.")
        @Test
        void should_AddAmountToUserPointAndSave_When_UserPointExists () {
            // given
            long id = 1L;
            long beforeAmount = 50L;
            long chargeAmount = 100L;
            UserPoint userPoint = new UserPoint(id, beforeAmount,
                System.currentTimeMillis());

            userPointRepository.insertOrUpdate(userPoint);

            // when
            PointDetail result = pointService.charge(id, chargeAmount);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getPointAmount()).isEqualTo(beforeAmount + chargeAmount);

            PointHistory pointHistory = pointHistoryRepository.selectAllByUserId(id).get(0);
            assertThat(pointHistory.userId()).isEqualTo(id);
            assertThat(pointHistory.amount()).isEqualTo(chargeAmount);
            assertThat(pointHistory.type()).isEqualTo(TransactionType.CHARGE);
        }
    }
}
