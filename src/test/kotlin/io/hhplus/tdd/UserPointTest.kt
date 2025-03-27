package io.hhplus.tdd

import io.hhplus.tdd.fixture.TestFixtures
import io.hhplus.tdd.point.UserPoint
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserPointTest {


    @Test
    fun `유저 포인트는 최대 100만 포인트를 넘을 수 없다 IllegalStateException`() {
        assertThatThrownBy {
            TestFixtures.userPoint(point = 1_000_001)
        }
            .isInstanceOf(IllegalStateException::class.java)
    }


    @Test
    fun `유저 포인트는 음수일 수 없다 IllegalStateException`() {
        assertThatThrownBy {
            TestFixtures.userPoint(point = -1)
        }
            .isInstanceOf(IllegalStateException::class.java)
    }


    @Test
    fun `유저 포인트는 0은 가능하다`() {
        assertThatCode {
            TestFixtures.userPoint(point = 0)
        }.doesNotThrowAnyException()
    }


    @Nested
    @DisplayName("포인트 사용 테스트")
    inner class use(){

        @Test
        fun `유저 포인트 사용이 100만 초과 할 수 없다 IllegalStateException`(){
            assertThatThrownBy {
                TestFixtures.userPoint(point = 1_000_000).use(1_000_001)
            }.isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `유저 포인트가 정상적으로 사용 된다`(){
            //given
            val userPoint = TestFixtures.userPoint(point = 1_000_000)

            //when
            val usedUserPoint = userPoint.use(1_000_000)

            //then
            assertThat(usedUserPoint.point).isEqualTo(0)
        }
    }



    @Nested
    @DisplayName("포인트 충전 테스트")
    inner class charge(){

        @Test
        fun `유저 포인트 충전이 0원 일 수 없다 IllegalStateException`(){
            assertThatThrownBy {
                TestFixtures.userPoint(point = 1000).charge(0)
            }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `유저 포인트가 정상적으로 충전 된다`(){
            // given
            val userPoint = TestFixtures.userPoint(point = 0)

            // when
            val chargedUserPoint = userPoint.charge(1000)

            // then
            assertThat(chargedUserPoint.point).isEqualTo(1000)
        }

        @Test
        fun `충전 후 포인트가 최대 1,000,000L를 넘지 않는 경계값 테스트`() {
            // 900,000L인 상태에서 100,000L 충전하면 1,000,000L가 되어야 함
            val userPoint = TestFixtures.userPoint(point = 900_000)
            val chargedUserPoint = userPoint.charge(100_000)
            assertThat(chargedUserPoint.point).isEqualTo(1_000_000)
        }
    }






}