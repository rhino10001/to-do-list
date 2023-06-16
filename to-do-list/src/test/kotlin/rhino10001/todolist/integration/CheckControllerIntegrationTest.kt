package rhino10001.todolist.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import rhino10001.todolist.dto.response.ExceptionResponse

@SpringBootTest
@AutoConfigureMockMvc
class CheckControllerIntegrationTest @Autowired constructor(

    private val mockMvc: MockMvc,

    private val objectMapper: ObjectMapper
) {

    @Test
    @WithAnonymousUser
    fun givenAnonymous_whenHello_thenReturnSucceed() {
        mockMvc
            .get("/api/v0/hello")
            .andExpect {
                content { string("Hello world!") }
            }
    }

    @Test
    @WithMockUser
    fun givenUser_whenHelloAuthenticated_thenReturnSucceed() {

        mockMvc
            .get("/api/v0/helloAuthenticated")
            .andExpect {
                status().`is`(200)
                content { string("Hello Authenticated!") }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenAnonymous_whenHelloAuthenticated_thenReturnError401() {

        val expectedResponse = ExceptionResponse(
            statusCode = 401,
            message = "Unauthorized"
        )

        mockMvc
            .get("/api/v0/helloAuthenticated")
            .andExpect {
                status().`is`(401)
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun givenAdmin_whenAdmin_thenReturnSucceed() {

        mockMvc
            .get("/api/v0/admin")
            .andExpect {
                status().`is`(200)
                content { string("Info Only For Admin!!") }
            }
    }

    @Test
    @WithMockUser
    fun givenUser_whenAdmin_thenReturnError403() {

        val expectedResponse = ExceptionResponse(
            statusCode = 403,
            message = "Forbidden"
        )

        mockMvc
            .get("/api/v0/admin")
            .andExpect {
                status { `is`(403) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenAnonymous_whenAdmin_thenReturnError401() {

        val expectedResponse = ExceptionResponse(
            statusCode = 401,
            message = "Unauthorized"
        )

        mockMvc
            .get("/api/v0/admin")
            .andExpect {
                status { `is`(401) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }
}