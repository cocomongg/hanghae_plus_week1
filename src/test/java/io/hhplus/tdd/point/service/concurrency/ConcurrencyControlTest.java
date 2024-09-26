package io.hhplus.tdd.point.service.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConcurrencyControlTest {

    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    @Autowired
    private UserPointRepository userPointRepository;
    @Autowired
    private PointValidator pointValidator;

    private ConcurrencyControlPointService pointService;

    @BeforeEach
    public void setup() {
//       pointService = new PointServiceWithSynchronized(userPointRepository,
//           pointHistoryRepository, pointValidator);

//        pointService = new PointServiceWithReentrantLock(userPointRepository,
//            pointHistoryRepository, pointValidator);

        pointService = new PointServiceWithSelectiveLock(userPointRepository,
            pointHistoryRepository, pointValidator);
    }

    @DisplayName("PointService 동시성 테스트")
    @Nested
    class PointServiceConcurrencyTest {
        @DisplayName("한 명의 유저에 대해서 충전이 동시에 이뤄질 경우 충전한 금액만큼 충전된다.")
        @Test
        void should_plusPointAsMuchAsCharge_When_ConcurrentChargedSameUser()
            throws InterruptedException {
            // given
            long userId = 1L;
            long chargeAmount = 100L;

            userPointRepository.insertOrUpdate(new UserPoint(userId, 0L, System.currentTimeMillis()));

            // when
            int executeCount = 50;
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
            int executeCount = 50;
            AtomicInteger counter = new AtomicInteger(0);
            this.executeConcurrentTask(executeCount, () -> {
                long userId = counter.getAndIncrement() % userIds.length + 1; // 각 userId당 5번씩 호출
                pointService.charge(userId, chargeAmount);
            });

            // then
            for(long userId : userIds) {
                Optional<UserPoint> userPointOptional = userPointRepository.selectById(userId);
                assertThat(userPointOptional).isPresent();

                UserPoint userPoint = userPointOptional.get();
                assertThat(userPoint.point())
                    .isEqualTo(chargeAmount * (executeCount / userIds.length)); /// 각 유저당 5번씩 반복
            }
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
