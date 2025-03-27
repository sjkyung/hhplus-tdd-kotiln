package io.hhplus.tdd.fixture

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import java.util.concurrent.CountDownLatch

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


    fun runConcurrently(count: Int, task: Runnable) {
        val latch = CountDownLatch(count) // 카운트를 `count`로 설정
        val threads = (1..count).map {
            Thread {
                task.run()  //실제 작업 실행
                latch.countDown()  //작업 완료 후 카운트 감소
            }
        }
        //모든 스레드를 시작
        threads.forEach { it.start() }
        //모든 스레드가 완료될 때까지 대기
        latch.await()
    }

    fun runTaskConcurrently(count: Int, vararg tasks: Runnable) {
        val latch = CountDownLatch(tasks.size)  //`tasks.size`만큼 카운트를 설정
        val threads = tasks.map { task ->  //여러 작업을 병렬로 실행
            Thread {
                task.run()  //실제 작업 실행
                latch.countDown()  //작업 완료 후 카운트 감소
            }
        }
        //모든 스레드를 시작
        threads.forEach { it.start() }
        //모든 스레드가 완료될 때까지 대기
        latch.await()
    }

}
