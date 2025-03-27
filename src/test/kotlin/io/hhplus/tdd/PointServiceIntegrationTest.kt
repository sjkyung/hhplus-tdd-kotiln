package io.hhplus.tdd

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.fixture.TestFixtures
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.springframework.test.annotation.DirtiesContext



@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PointServiceIntegrationTest @Autowired constructor(
    private val pointService: PointService,
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {


    @Test
    fun `유저 포인트를 조회 할 수 있다`(){
        //given
        val userPoint = UserPoint(1,1000, System.currentTimeMillis())
        userPointTable.insertOrUpdate(userPoint.id, userPoint.point)

        //when
        val result = pointService.getUserPoint(userPoint.id)

        //then
        assertThat(result.id).isEqualTo(userPoint.id)
        assertThat(result.point).isEqualTo(userPoint.point)
    }


    @Test
    fun `유저의 포인트 변경 이력을 조회할 수 있다`(){
        //given
        val userId = 1L
        val pointHistories = listOf(
            PointHistory(1,userId,TransactionType.CHARGE,1000,System.currentTimeMillis()),
            PointHistory(2,userId,TransactionType.CHARGE,1000,System.currentTimeMillis()),
            PointHistory(3,userId,TransactionType.USE,1000,System.currentTimeMillis()),
        ).map { pointHistory ->
            pointHistoryTable.insert(
                id = pointHistory.userId,
                amount = pointHistory.amount,
                transactionType = pointHistory.type,
                updateMillis = pointHistory.timeMillis,
            )
        }.toList()

        //when
        val result = pointService.getPointHistory(userId)

        //then
        assertThat(result).hasSize(pointHistories.size).extracting("userId", "amount", "type", "timeMillis")
            .containsExactlyInAnyOrder(
                tuple(userId, pointHistories[0].amount, pointHistories[0].type, pointHistories[0].timeMillis),
                tuple(userId, pointHistories[1].amount, pointHistories[1].type, pointHistories[1].timeMillis),
                tuple(userId, pointHistories[2].amount, pointHistories[2].type, pointHistories[2].timeMillis),
            )
    }

    @Test
    fun `유저의 포인트를 충전 할 수 있다`(){
        //given
        val userId = 1L
        val amount = 1000L

        //when
        val chargedPoint = pointService.charge(userId, amount)

        //then
        assertThat(chargedPoint.id).isEqualTo(userId)
        assertThat(chargedPoint.point).isEqualTo(amount)
    }

    @Test
    fun `유저의 포인트 충전시 포인트 충전 내역이 저장되어야 한다`(){
        //given
        val userId = 1L
        val amount = 1000L

        //when
        pointService.charge(userId, amount)
        val pointHistories = pointHistoryTable.selectAllByUserId(userId)

        //then
        assertThat(pointHistories)
            .hasSize(1)
            .extracting("userId", "amount", "type")
            .containsExactlyInAnyOrder(
                tuple(userId, amount, TransactionType.CHARGE),
            )
    }

    @Test
    fun `유저의 포인트를 사용할 수 있다`(){
        // given
        val userPoint = UserPoint(1,3000,System.currentTimeMillis())
        userPointTable.insertOrUpdate(userPoint.id, userPoint.point)
        val usePoint = 2000L

        // when
        val usedPoint = pointService.use(userPoint.id, usePoint)

        // then
        assertThat(usedPoint.id).isEqualTo(userPoint.id)
        assertThat(usedPoint.point).isEqualTo(userPoint.point - usePoint)
    }

    @Test
    fun `유저의 포인트 사용시 포인트 사용 내역이 저장되어야 한다`(){
        //given
        val userId = 1L
        val amount = 1000L
        userPointTable.insertOrUpdate(userId, amount)

        //when
        pointService.use(userId, amount)
        val pointHistories = pointHistoryTable.selectAllByUserId(userId)

        //then
        assertThat(pointHistories)
            .hasSize(1)
            .extracting("userId", "amount", "type")
            .containsExactlyInAnyOrder(
                tuple(userId, amount, TransactionType.USE),
            )
    }

    @Test
    fun `하나의 유저에게 동시에 포인트 사용 요청이 들어오더라도 오차 없이 사용되어야 한다`() {
        // given
        val userId = 1L
        val usePoint = 100L
        val amount = 10000L
        val taskCount = 100
        userPointTable.insertOrUpdate(userId, amount)

        //when
        TestFixtures.runConcurrently(taskCount) {
            pointService.use(userId, usePoint)
        }
        val result = userPointTable.selectById(userId)

        //then
        assertThat(result.point).isEqualTo(amount - (usePoint * taskCount))
    }


    @Test
    fun `하나의 유저에게 동시에 포인트 충전 요청이 들어오더라도 충전되어야 한다`() {
        //given
        val userId = 1L
        val chargeAmount = 100L
        val taskCount = 100

        //when
        TestFixtures.runConcurrently(taskCount) {
            pointService.charge(userId, chargeAmount)
        }
        val result = userPointTable.selectById(userId)

        //then
        assertThat(result.point).isEqualTo(chargeAmount * taskCount)
    }


    @Test
    fun `하나의 유저에게 동시에 포인트 충전과 사용 요청이 들어오더라도 올바르게 처리되어야 한다`() {
        //given
        val userId = 1L
        val chargeAmount = 100L
        val usePoint = 100L
        val taskCount = 100
        val amount = 10000L

        //초기 포인트 설정
        userPointTable.insertOrUpdate(userId, amount)

        //when
        TestFixtures.runConcurrently(taskCount) {
            // 각 스레드에서 포인트 충전과 사용을 병행
            pointService.charge(userId, chargeAmount)
            pointService.use(userId, usePoint)
        }

        //포인트 사용 후 최종 결과
        val result = userPointTable.selectById(userId)

        //then
        //충전된 포인트와 사용된 포인트의 합이 예상 결과와 일치해야 함
        val expectedFinalPoint = amount + (chargeAmount * taskCount) - (usePoint * taskCount)
        assertThat(result.point).isEqualTo(expectedFinalPoint)
    }



}