package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service


@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) {

    /**
     * 유저의 point 조회
     */
    fun getUserPoint(
        userId :Long
    ): UserPoint {
       return userPointTable.selectById(userId)
    }

    /**
     * 유저의 포인트 history 조회
     */
    fun getPointHistory(
        userId :Long
    ): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }

    /**
     * 포인트 충전, history 추가
     */
    fun charge(
        userId:Long,
        amount:Long
    ): UserPoint {

        val userPoint = userPointTable.selectById(userId)
        val updatedUserPoint = userPoint.charge(amount)
        val persistedUserPoint = userPointTable.insertOrUpdate(
            userId,
            updatedUserPoint.point
        )

        pointHistoryTable.insert(
            userId,
            amount,
            TransactionType.CHARGE,
            persistedUserPoint.updateMillis
        )

        return updatedUserPoint;
    }

    /**
     * 포인트 사용, history 추가
     */
    fun use(
        userId:Long,
        amount:Long
    ): UserPoint {

        val userPoint = userPointTable.selectById(userId)
        val updatedUserPoint = userPoint.use(amount)
        val persistedUserPoint = userPointTable.insertOrUpdate(userId, updatedUserPoint.point)

        pointHistoryTable.insert(
            userId,
            amount,
            TransactionType.USE,
            persistedUserPoint.updateMillis
        )

        return persistedUserPoint;
    }

}