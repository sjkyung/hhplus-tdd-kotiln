package io.hhplus.tdd

import io.hhplus.tdd.point.*
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@WebMvcTest(PointController::class)
class PointControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var pointService: PointService


    @Test
    fun `포인트 조회 GET 200 Ok`(){
        //given
        val userPoint = UserPoint(1L,1000L,System.currentTimeMillis())
        given(pointService.getUserPoint(1L))
            .willReturn(userPoint)

        //when & then
        mockMvc.perform(
                get("/point/{id}", 1L)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(userPoint.id))
            .andExpect(jsonPath("point").value(userPoint.point))
            .andExpect(jsonPath("updateMillis").value(userPoint.updateMillis))
    }


    @Test
    fun `포인트 내역 조회 history GET 200 Ok`(){
        //given
        val pointHistory = listOf(
            PointHistory(1L,1L,TransactionType.CHARGE,1000L,System.currentTimeMillis()),
            PointHistory(2L,1L,TransactionType.CHARGE,1000L,System.currentTimeMillis()),
            PointHistory(3L,1L,TransactionType.USE,2000L,System.currentTimeMillis())
        )
        given(pointService.getPointHistory(1L))
            .willReturn(pointHistory)

        //when & then
        mockMvc.perform(
            get("/point/{id}/histories", 1L)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("[0].id").value(pointHistory[0].id))
            .andExpect(jsonPath("[0].userId").value(pointHistory[0].userId))
            .andExpect(jsonPath("[0].type").value(pointHistory[0].type.name))
            .andExpect(jsonPath("[0].amount").value(pointHistory[0].amount))
            .andExpect(jsonPath("[0].timeMillis").value(pointHistory[0].timeMillis))
            .andExpect(jsonPath("[1].id").value(pointHistory[1].id))
            .andExpect(jsonPath("[1].userId").value(pointHistory[1].userId))
            .andExpect(jsonPath("[1].type").value(pointHistory[1].type.name))
            .andExpect(jsonPath("[1].amount").value(pointHistory[1].amount))
            .andExpect(jsonPath("[1].timeMillis").value(pointHistory[1].timeMillis))
    }

    @Test
    fun `포인트 충전 charge PATCH 200 Ok`(){
        //given
        val userPoint = UserPoint(1L,1000L,System.currentTimeMillis())
        given(pointService.charge(1L, 1000L))
            .willReturn(userPoint)

        //when & then
        mockMvc.perform(
            patch("/point/{id}/charge", 1L)
                .content("1000")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(userPoint.id))
            .andExpect(jsonPath("point").value(userPoint.point))
            .andExpect(jsonPath("updateMillis").value(userPoint.updateMillis))
    }


    @Test
    fun `포인트 사용 use PATCH 200 Ok`(){
        //given
        val userPoint = UserPoint(1L,1000L,System.currentTimeMillis())
        given(pointService.use(1L, 1000L))
            .willReturn(userPoint)

        //when & then
        mockMvc.perform(
            patch("/point/{id}/use", 1L)
                .content("1000")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(userPoint.id))
            .andExpect(jsonPath("point").value(userPoint.point))
            .andExpect(jsonPath("updateMillis").value(userPoint.updateMillis))
    }

}