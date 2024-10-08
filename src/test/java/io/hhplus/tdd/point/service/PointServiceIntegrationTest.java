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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
    
    @DisplayName("포인트 사용 통합 테스트 - use()")
    @Nested
    class UseIntegrationTest {
        @DisplayName("포인트 사용 금액이 음수라면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_AmountIsNegative () {
            // given
            long negativeAmount = -100L;

            // when, then
            assertThatThrownBy(() -> pointService.use(0L, negativeAmount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.INVALID_POINT_AMOUNT.getMessage());
        }

        @DisplayName("포인트 사용 금액이 0이라면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_AmountIsZero () {
            // given
            long zeroAmount = 0L;

            // when, then
            assertThatThrownBy(() -> pointService.use(0L, zeroAmount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.INVALID_POINT_AMOUNT.getMessage());
        }

        @DisabledIf("isUserPointRepositoryUseTable")
        @DisplayName("id에 해당하는 UserPoint가 없으면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_WhenUserPointNotFound() {
            // given
            long notExistId = -1L;

            // when, then
            assertThatThrownBy(() -> pointService.use(notExistId, 100L))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.NOT_FOUND_USER_POINT.getMessage());
        }

        @DisplayName("잔액이 부족하면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_WhenPointInsufficient() {
            // given
            long id = 0L;
            long balanceAmount = 50L;
            long useAmount = 100L;

            UserPoint userPoint = new UserPoint(id, balanceAmount, System.currentTimeMillis());
            userPointRepository.insertOrUpdate(userPoint);

            // when, then
            assertThatThrownBy(() -> pointService.use(id, useAmount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.INSUFFICIENT_POINT_BALANCE.getMessage());
        }

        @DisplayName("포인트 사용을 하면 사용한 만큼 차감된 UserPoint가 업데이트되고, PointHistory에 내역을 저장한다.")
        @Test
        void should_MinusAndUpdateUserPoint_AndSavePointHistory_When_Use() {
            // given
            long id = 0L;
            long balanceAmount = 100L;
            long useAmount = 50L;

            UserPoint userPoint = new UserPoint(id, balanceAmount, System.currentTimeMillis());
            userPointRepository.insertOrUpdate(userPoint);

            // when
            PointDetail result = pointService.use(id, useAmount);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getPointAmount()).isEqualTo(balanceAmount - useAmount);

            PointHistory pointHistory = pointHistoryRepository.selectAllByUserId(id).get(0);
            assertThat(pointHistory.userId()).isEqualTo(id);
            assertThat(pointHistory.amount()).isEqualTo(useAmount);
            assertThat(pointHistory.type()).isEqualTo(TransactionType.USE);
        }

        boolean isUserPointRepositoryUseTable() {
            return userPointRepository instanceof UserPointInMemoryRepository;
        }
    }

    @DisplayName("포인트 충전/사용 동시성 테스트 - concurrency")
    @Nested
    class ChargeAndUseConcurrencyTest {
        @DisplayName("한 명의 유저에 대해서 충전이 동시에 이뤄질 경우 충전한 금액만큼 충전된다.")
        @Test
        void should_plusPointAsMuchAsCharge_When_ConcurrentChargedSameUser()
            throws InterruptedException {
            // given
            long userId = 1L;
            long chargeAmount = 100L;

            userPointRepository.insertOrUpdate(new UserPoint(userId, 0L, System.currentTimeMillis()));

            // when
            int executeCount = 20;
            this.executeConcurrentTask(executeCount, () -> pointService.charge(userId, chargeAmount));

            // then
            Optional<UserPoint> userPointOptional = userPointRepository.selectById(userId);
            assertThat(userPointOptional).isPresent();

            UserPoint userPoint = userPointOptional.get();
            assertThat(userPoint.point()).isEqualTo( chargeAmount * executeCount);
        }

        @DisplayName("여러 명의 유저에 대해서 충전이 동시에 이뤄질 경우 각 유저의 충전 금액만큼 충전된다.")
        @Test
        void should_plusPointAsMuchAsCharge_When_ConcurrentChargedMultipleUser ()
            throws InterruptedException {
            // given
            long[] userIds = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            long chargeAmount = 100L;

            for (long userId : userIds) {
                userPointRepository
                    .insertOrUpdate(new UserPoint(userId, 0L, System.currentTimeMillis()));
            }

            // when
            int executeCount = 20;
            AtomicInteger counter = new AtomicInteger(0);
            this.executeConcurrentTask(executeCount, () -> {
                long userId = counter.getAndIncrement() % userIds.length + 1; // 각 userId당 2번씩 호출
                pointService.charge(userId, chargeAmount);
            });

            // then
            for(long userId : userIds) {
                Optional<UserPoint> userPointOptional = userPointRepository.selectById(userId);
                assertThat(userPointOptional).isPresent();

                UserPoint userPoint = userPointOptional.get();
                assertThat(userPoint.point()).isEqualTo(chargeAmount * 2); /// 각 유저당 2번씩 반복
            }
        }

        @DisplayName("한 명의 유저에 대해서 사용이 동시에 이뤄질 경우 사용한 금액만큼 사용된다.")
        @Test
        void should_MinusPointAsMuchAsUse_When_ConcurrentUseSameUser() throws InterruptedException {
            // given
            long userId = 1L;
            long balanceAmount = 10_000L;
            long useAmount = 100L;

            userPointRepository.insertOrUpdate(new UserPoint(userId, balanceAmount, System.currentTimeMillis()));

            // when
            int executeCount = 20;
            this.executeConcurrentTask(executeCount, () -> pointService.use(userId, useAmount));

            // then
            Optional<UserPoint> userPointOptional = userPointRepository.selectById(userId);
            assertThat(userPointOptional).isPresent();

            UserPoint userPoint = userPointOptional.get();
            assertThat(userPoint.point())
                .isEqualTo( balanceAmount - useAmount * executeCount);
        }

        @DisplayName("여러 명의 유저에 대해서 사용이 동시에 이뤄질 경우 각 유저의 사용금액만큼 사용된다.")
        @Test
        void should_MinusPointAsMuchAsUse_When_ConcurrentUseMultipleUser ()
            throws InterruptedException {
            // given
            long[] userIds = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            long balanceAmount = 10_000L;
            long useAmount = 100L;

            for (long userId : userIds) {
                userPointRepository
                    .insertOrUpdate(new UserPoint(userId, balanceAmount, System.currentTimeMillis()));
            }

            // when
            int executeCount = 20;
            AtomicInteger counter = new AtomicInteger(0);
            this.executeConcurrentTask(executeCount, () -> {
                long userId = counter.getAndIncrement() % userIds.length + 1; // 각 userId당 2번씩 호출
                pointService.use(userId, useAmount);
            });

            // then
            for(long userId : userIds) {
                Optional<UserPoint> userPointOptional = userPointRepository.selectById(userId);
                assertThat(userPointOptional).isPresent();

                UserPoint userPoint = userPointOptional.get();
                assertThat(userPoint.point()).isEqualTo(balanceAmount - useAmount * 2); // 각 유저당 2번씩 반복
            }
        }

        @DisplayName("한 명의 유저에 대해서 충전과 사용이 동시에 이뤄질 경우 해당 금액만큼 충전되고 사용된다.")
        @Test
        void should_CorrectAmount_When_ConcurrentChargeAndUseSameUser()
            throws InterruptedException {
            // given
            long userId = 1L;
            long balanceAmount = 1_000L;
            long chargeAmount = 100L;
            long useAmount = 50L;

            userPointRepository.insertOrUpdate(new UserPoint(userId, balanceAmount, System.currentTimeMillis()));

            // when
            int executeCount = 20;
            AtomicInteger counter = new AtomicInteger(0);
            this.executeConcurrentTask(executeCount, () -> {
                if(counter.getAndIncrement() % 2 == 0) { // charge와 use를 섞어서 절반씩 호출
                    pointService.charge(userId, chargeAmount);
                } else {
                    pointService.use(userId, useAmount);
                }
            });

            // then
            Optional<UserPoint> userPointOptional = userPointRepository.selectById(userId);
            assertThat(userPointOptional).isPresent();

            UserPoint userPoint = userPointOptional.get();
            assertThat(userPoint.point()).isEqualTo(balanceAmount
                + chargeAmount * (executeCount / 2) - useAmount * (executeCount / 2)
            );
        }

        private void executeConcurrentTask(int executeCount, Runnable task) throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(executeCount);
            CountDownLatch latch = new CountDownLatch(executeCount);

            for(int i = 0; i < executeCount; ++i) {
                executorService.submit(() -> {
                    try {
                        task.run();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();
        }
    }
}
