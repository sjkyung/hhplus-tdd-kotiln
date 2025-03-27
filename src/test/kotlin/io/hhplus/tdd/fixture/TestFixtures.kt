package io.hhplus.tdd.fixture

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint

object TestFixtures {

    fun userPoint(
        id: Long = 1L,
        point: Long = 100L,
        updateMillis: Long = System.currentTimeMillis(),
    ): UserPoint = UserPoint(
        id = id,
        point = point,
        updateMillis = updateMillis,
    )

    fun pointHistory(
        id: Long = 1L,
        userId: Long = 1L,
        type: TransactionType = TransactionType.CHARGE,
        amount: Long = 100L,
        timeMillis: Long = System.currentTimeMillis(),
    ): PointHistory = PointHistory(
        id = id,
        userId = userId,
        type = type,
        amount = amount,
        timeMillis = timeMillis,
    )
}
