package io.hhplus.tdd

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.fixture.TestFixtures
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.UserPoint
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*


class PointServiceUnitTest {

    private lateinit var pointService: PointService
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable

    @BeforeEach
    fun init() {
        userPointTable = mock(UserPointTable::class.java)
        pointHistoryTable = mock(PointHistoryTable::class.java)
        pointService = PointService(userPointTable,pointHistoryTable)
    }

    @Test
    fun `포인트서비스 유저의 포인트를 조회한다`(){
        //given
        val userPoint = TestFixtures.userPoint()

        given(userPointTable.selectById(anyLong())).willReturn(userPoint)

        //when
        val currentUserPoint = pointService.getUserPoint(userPoint.id);

        //then
        assertThat(currentUserPoint.id).isEqualTo(userPoint.id)
        assertThat(currentUserPoint.point).isEqualTo(userPoint.point)

        verify(userPointTable, times(1)).selectById(userPoint.id)
    }


    @Test
    fun `포인트서비스 유저의 포인트내역을 조회한다`(){
        //given
        val userId = 1L
        val pointHistories = listOf(
            TestFixtures.pointHistory(userId = userId, amount = 1000),
            TestFixtures.pointHistory(userId = userId, amount = 2000),
            TestFixtures.pointHistory(userId = userId, amount = 3000),
        )

        given(pointHistoryTable.selectAllByUserId(anyLong())).willReturn(pointHistories)

        //when
        val pointHistory = pointService.getPointHistory(userId)

        //then
        assertThat(pointHistory.size).isEqualTo(pointHistories.size)
        assertThat(pointHistory).usingRecursiveComparison().isEqualTo(pointHistories)

        verify(pointHistoryTable, times(1)).selectAllByUserId(userId)
    }


    @Test
    fun `유저의 포인트를 정상적인 충전합니다`() {
        //given
        val userId = 1L
        val amount = 1000L
        val chargeAmount = 2000L
        val userPoint = TestFixtures.userPoint(userId, amount)

        given(userPointTable.selectById(userId)).willReturn(userPoint)
        given(userPointTable.insertOrUpdate(userId, amount + chargeAmount)).willReturn(
            UserPoint(
                userId,
                amount + chargeAmount,
                System.currentTimeMillis()
            )
        )

        //when
        val chargeUserPoint = pointService.charge(userId, chargeAmount)

        //then
        assertThat(chargeUserPoint.point).isEqualTo(amount + chargeAmount)

        verify(userPointTable, times(1)).selectById(userId)
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount + chargeAmount)
    }


    @Test
    fun `유저의 포인트를 정상적인 사용합니다`() {
        //given
        val userId = 1L
        val amount = 2000L
        val useAmount = 1000L
        val userPoint = TestFixtures.userPoint(userId, amount)

        given(userPointTable.selectById(userId)).willReturn(userPoint)
        given(userPointTable.insertOrUpdate(userId, amount - useAmount)).willReturn(
            UserPoint(
                userId,
                amount - useAmount,
                System.currentTimeMillis()
            )
        )

        //when
        val chargeUserPoint = pointService.use(userId, useAmount)

        //then
        assertThat(chargeUserPoint.point).isEqualTo(amount - useAmount)

        verify(userPointTable, times(1)).selectById(userId)
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount - useAmount)
    }

    @Test
    fun `포인트를 충전이 최대 한도를 초과했다 IllegalStateException`() {
        //given
        val userId = 1L
        //최대 한도가 1,000,000L라면, 현재 포인트가 900,000L인 상황에서 200,000L 충전 시도를 테스트
        val currentPoint = 900_000L
        val chargeAmount = 200_000L
        val userPoint = TestFixtures.userPoint(userId, currentPoint)

        given(userPointTable.selectById(userId)).willReturn(userPoint)

        //when & then
        assertThatThrownBy { pointService.charge(userId, chargeAmount) }
            .isInstanceOf(IllegalStateException::class.java)

        verify(userPointTable, times(1)).selectById(userId)
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong())
    }

    @Test
    fun `포인트를 사용한다 남은 포인트가 없다 IllegalStateException`() {
        // given
        val userId = 1L
        val currentPoint = 500L
        val useAmount = 600L // 사용하려는 금액이 현재 보유한 포인트보다 많음
        val userPoint = TestFixtures.userPoint(userId, currentPoint)

        given(userPointTable.selectById(userId)).willReturn(userPoint)

        //when & then
        assertThatThrownBy { pointService.use(userId, useAmount) }
            .isInstanceOf(IllegalStateException::class.java)

        verify(userPointTable, times(1)).selectById(userId)
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong())
    }




}