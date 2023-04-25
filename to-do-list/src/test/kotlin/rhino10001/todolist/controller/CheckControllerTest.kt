package rhino10001.todolist.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import rhino10001.todolist.configuration.SpringSecurityConfiguration
import rhino10001.todolist.dto.response.ExceptionResponse
import rhino10001.todolist.security.JwtTokenProvider


@WebMvcTest(CheckController::class)
@Import(SpringSecurityConfiguration::class)
class CheckControllerTest @Autowired constructor(

    private val mockMvc: MockMvc,

    private val objectMapper: ObjectMapper,

    @MockBean
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Test
    @WithAnonymousUser
    fun givenAnonymous_whenHello_thenReturnSucceed() {
        mockMvc
            .perform(get("/api/v0/hello"))
            .andExpect(content().string("Hello world!"))
    }

    @Test
    @WithMockUser
    fun givenUser_whenHelloAuthenticated_thenReturnSucceed() {

        mockMvc
            .perform(get("/api/v0/helloAuthenticated"))
            .andExpect(status().`is`(200))
            .andExpect(content().string("Hello Authenticated!"))
    }

    @Test
    @WithAnonymousUser
    fun givenAnonymous_whenHelloAuthenticated_thenReturnError401() {

        val expectedResponse = ExceptionResponse(
            statusCode = 401,
            message = "Unauthorized"
        )

        mockMvc
            .perform(get("/api/v0/helloAuthenticated"))
            .andExpect(status().`is`(401))
            .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)))
    }

    @Test
    @WithMockUser(authorities = ["ADMIN"])
    fun givenAdmin_whenAdmin_thenReturnSucceed() {

        mockMvc
            .perform(get("/api/v0/admin"))
            .andExpect(status().`is`(200))
            .andExpect(content().string("Info Only For Admin!!"))
    }

    @Test
    @WithMockUser
    fun givenUser_whenAdmin_thenReturnError403() {

        val expectedResponse = ExceptionResponse(
            statusCode = 403,
            message = "Forbidden"
        )

        mockMvc
            .perform(get("/api/v0/admin"))
            .andExpect(status().`is`(403))
            .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)))
    }

    @Test
    @WithAnonymousUser
    fun givenAnonymous_whenAdmin_thenReturnError401() {

        val expectedResponse = ExceptionResponse(
            statusCode = 401,
            message = "Unauthorized"
        )

        mockMvc
            .perform(get("/api/v0/admin"))
            .andExpect(status().`is`(401))
            .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)))
    }

}