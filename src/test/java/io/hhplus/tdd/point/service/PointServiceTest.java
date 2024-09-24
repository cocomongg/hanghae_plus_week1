package io.hhplus.tdd.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.dto.PointDto.PointHistoryDetail;
import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.validator.PointValidator;
import java.util.Collections;
import java.util.List;
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
    private PointValidator pointValidator;

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    @DisplayName("포인트 조회 - getUserPoint() 테스트")
    @Nested
    class GetUserPointTest {
        @DisplayName("id에 해당하는 UserPoint가 존재하지 않으면 PointException이 발생한다.")
        @Test
        void should_ThrowPointException_When_NotFound() {
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

        @DisplayName("존재하는 id에 해당하는 UserPoint dto(PointDetail)를 반환한다.")
        @Test
        void should_ReturnPointDetail_When_Found() {
            // given
            long existId = 0L;
            UserPoint expectedUserPoint =
                new UserPoint(existId, 100L, System.currentTimeMillis());

            // when
            when(userPointRepository.selectById(existId))
                .thenReturn(Optional.of(expectedUserPoint));

            // then
            PointDetail pointDetail = pointService.getUserPoint(existId);
            assertThat(pointDetail.getId())
                .isEqualTo(existId);
            assertThat(pointDetail.getPointAmount())
                .isEqualTo(expectedUserPoint.point());
            assertThat(pointDetail.getUpdateMillis())
                .isEqualTo(expectedUserPoint.updateMillis());
        }
    }

    @DisplayName("포인트 내역 조회 - getPointHistories() 테스트")
    @Nested
    class GetPointHistoriesTest {
        @DisplayName("userId에 해당하는 내역이 없으면 빈 리스트를 반환한다.")
        @Test
        void should_ReturnEmptyList_When_NotFound() {
            // given
            long notExistUserId = -1L;

            when(pointHistoryRepository.selectAllByUserId(notExistUserId))
                .thenReturn(Collections.emptyList());

            // when
            List<PointHistoryDetail> pointHistoryDetails = pointService.getUserPointHistories(
                notExistUserId);

            // then
            assertThat(pointHistoryDetails).isEmpty();
        }

        @DisplayName("userId에 해당하는 PointHistory dto(PointHistoryDetail)목록을 반환한다.")
        @Test
        void should_ReturnHistoryDetailList_When_Found() {
            // given
            long existUserId = 0L;

            List<PointHistory> expectedPointHistories = List.of(
                new PointHistory(0L, existUserId, 100L, TransactionType.CHARGE,
                    System.currentTimeMillis()),
                new PointHistory(0L, existUserId, 50L, TransactionType.USE,
                    System.currentTimeMillis())
            );

            when(pointHistoryRepository.selectAllByUserId(existUserId))
                .thenReturn(expectedPointHistories);

            // when
            List<PointHistoryDetail> pointHistoryDetails = pointService
                .getUserPointHistories(existUserId);

            // then
            assertThat(pointHistoryDetails)
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
    
    @DisplayName("포인트 충전 - charge() 테스트")
    @Nested
    class ChargeTest {
        @DisplayName("처음 충전 시 입력받은 정보로 UserPoint를 저장한다.")
        @Test
        void should_SaveUserPoint_When_FirstCharge() {
            // given
            long firstChargerId = 0L;
            long chargeAmount = 100L;
            UserPoint expectedUserPoint = new UserPoint(firstChargerId, chargeAmount,
                System.currentTimeMillis());

            doNothing().when(pointValidator).checkAmount(chargeAmount);

            when(userPointRepository.selectById(firstChargerId))
                .thenReturn(Optional.empty());

            when(userPointRepository.insertOrUpdate(any(UserPoint.class)))
                .thenReturn(expectedUserPoint);

            // when
            PointDetail chargeResult = pointService.charge(firstChargerId, chargeAmount);

            // then
            assertThat(chargeResult.getId())
                .isEqualTo(firstChargerId);
            assertThat(chargeResult.getPointAmount())
                .isEqualTo(chargeAmount);
            assertThat(chargeResult.getUpdateMillis())
                .isEqualTo(expectedUserPoint.updateMillis());
        }

        @DisplayName("존재하는 id의 UserPoint에 amount만큼 더하고 저장한다.")
        @Test
        void should_AddAmountToUserPointAndSave_When_UserPointExists() {
            // given
            long existId = 1L;
            long beforeAmount = 50L;
            long chargeAmount = 100L;
            UserPoint existUserPoint = new UserPoint(existId, beforeAmount,
                System.currentTimeMillis());

            UserPoint expectedUserPoint = new UserPoint(existId, beforeAmount + chargeAmount,
                System.currentTimeMillis());

            doNothing().when(pointValidator).checkAmount(chargeAmount);

            when(userPointRepository.selectById(existId))
                .thenReturn(Optional.of(existUserPoint));

            when(userPointRepository.insertOrUpdate(any(UserPoint.class)))
                .thenReturn(expectedUserPoint);

            // when
            PointDetail chargeResult = pointService.charge(existId, chargeAmount);

            // then
            assertThat(chargeResult.getId())
                .isEqualTo(existId);
            assertThat(chargeResult.getPointAmount())
                .isEqualTo(beforeAmount + chargeAmount);
            assertThat(chargeResult.getUpdateMillis())
                .isEqualTo(expectedUserPoint.updateMillis());
        }

        @DisplayName("충전을 완료한 뒤에 PointHistory를 저장한다.")
        @Test
        void should_SavePointHistory_When_AfterChargeFinish() {
            // given
            long id = 0L;
            long chargeAmount = 100L;
            UserPoint userPoint = new UserPoint(id, chargeAmount, System.currentTimeMillis());

            doNothing().when(pointValidator).checkAmount(chargeAmount);

            when(userPointRepository.selectById(id))
                .thenReturn(Optional.empty());

            when(userPointRepository.insertOrUpdate(any(UserPoint.class)))
                .thenReturn(userPoint);

            // when
            pointService.charge(id, chargeAmount);

            // then
            verify(pointHistoryRepository)
                .insert(PointHistory.createChargeHistory(id, chargeAmount, userPoint.updateMillis()));
        }
    }
}