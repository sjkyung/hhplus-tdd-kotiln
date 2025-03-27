package io.hhplus.tdd

import io.hhplus.tdd.fixture.TestFixtures
import io.hhplus.tdd.point.UserPointLocker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserPointLockerTest {

    @Test
    fun `같은 유저의 경우 잠금을 통해 동시 실행을 제어할 수 있다`() {
        //given
        val userId = 1L
        val task = {
            UserPointLocker.withUserPointLock(userId) {
                Thread.sleep(1000)
            }
        }

        //when
        val startTime = System.currentTimeMillis()
        TestFixtures.runConcurrently(2, task)// 동시 2번 실행
        val endTime = System.currentTimeMillis()

        //then
        val resultTime = endTime - startTime
        assertThat(resultTime).isGreaterThanOrEqualTo(2000)
    }


    @Test
    fun `다른 유저의 경우 잠금과 관계없이 동시 실행이 가능해야 한다`() {
        //given
        val task1 = {
            //유저 1의 작업
            UserPointLocker.withUserPointLock(1L) {
                Thread.sleep(1000) // 1초 대기
            }
        }
        val task2 = {
            //유저 2의 작업
            UserPointLocker.withUserPointLock(2L) {
                Thread.sleep(500) // 0.5초 대기
            }
        }

        //when
        val startTime = System.currentTimeMillis()
        TestFixtures.runTaskConcurrently(2, task1, task2)  // 두 개의 다양한 작업을 병렬로 실행
        val endTime = System.currentTimeMillis()

        //then
        val resultTime = endTime - startTime
        assertThat(resultTime).isLessThan(2000)
    }
}