package rhino10001.todolist.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rhino10001.todolist.dto.request.LoginRequest
import rhino10001.todolist.dto.request.RefreshRequest
import rhino10001.todolist.dto.request.RegistrationRequest
import rhino10001.todolist.dto.response.LoginResponse
import rhino10001.todolist.repository.UserRepository

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application.yml"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationControllerIntegrationTest @Autowired constructor(

    private val mockMvc: MockMvc,

    private val objectMapper: ObjectMapper,

    private val userRepository: UserRepository,

    @Value("\${jwt.refreshToken.expirationTime}")
    private val refreshExpirationTime: Long
) {

    private val testUsername = "test_username"
    private val testPassword = "test_password"

    @BeforeAll
    fun givenNewUser_whenRegister_thenReturnsCreated() {

        try {
            val testUser = userRepository.findByUsername(testUsername)
            userRepository.delete(testUser)
        } catch (ignored: EmptyResultDataAccessException) {}

//        given
        val userRequest = RegistrationRequest(
            username = testUsername,
            password = testPassword
        )

//        when
        val result = mockMvc.perform(
            post("/api/v0/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
        )

//        then
        result
            .andExpect(status().`is`(201))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue(Long::class.java)))
            .andExpect(jsonPath("$.username", `is`(userRequest.username)))
    }

    @Test
    fun givenExistingUser_whenRegister_thenReturnsError422() {

//        given
        val userRequest = RegistrationRequest(
            username = testUsername,
            password = testPassword
        )

//        when
        val result = mockMvc.perform(
            post("/api/v0/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
        )

//        then
        result
            .andExpect(status().`is`(422))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.statusCode", `is`(422)))
            .andExpect(jsonPath("$.message", `is`("User with such username is already registered")))
    }

    @Test
    fun givenUserWithCorrectData_whenLogin_thenSucceed() {

//        given
        val userRequest = LoginRequest(
            username = testUsername,
            password = testPassword
        )

//        when
        val result = mockMvc.perform(
            post("/api/v0/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
        )

//        then
        result
            .andExpect(status().`is`(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username", `is`(userRequest.username)))
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
    }

    @Test
    fun givenUserWithUnknownUsername_whenLogin_thenReturnsError401() {

//        given
        val userRequest = LoginRequest(
            username = "test_unknown_username",
            password = testPassword
        )

//        when
        val result = mockMvc.perform(
            post("/api/v0/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
        )

//        then
        result
            .andExpect(status().`is`(401))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.statusCode", `is`(401)))
            .andExpect(jsonPath("$.message", `is`("Incorrect username")))
    }

    @Test
    fun givenUserWithIncorrectPassword_whenLogin_thenReturnsError401() {

//        given
        val userRequest = LoginRequest(
            username = testUsername,
            password = "test_incorrect_password"
        )

//        when
        val result = mockMvc.perform(
            post("/api/v0/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
        )

//        then
        result
            .andExpect(status().`is`(401))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.statusCode", `is`(401)))
            .andExpect(jsonPath("$.message", `is`("Incorrect password")))
    }

    @Test
    fun givenValidRefreshToken_whenRefresh_thenReturnsSucceed() {

//        given
        val loginRequest = LoginRequest(
            username = testUsername,
            password = testPassword
        )

        val loginResult = mockMvc.perform(
            post("/api/v0/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )

        var refreshToken = ""
        loginResult
            .andExpect(status().`is`(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andDo {
                refreshToken = objectMapper
                    .readValue(it.response.contentAsString, LoginResponse::class.java)
                    .refreshToken
            }

        val userRequest = RefreshRequest(refreshToken = refreshToken)

//        when
        val result = mockMvc.perform(
            post("/api/v0/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
        )

//        then
        result
            .andExpect(status().`is`(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
    }

    @Test
    fun givenInvalidRefreshToken_whenRefresh_thenReturnsError401() {

//        given
        val loginRequest = LoginRequest(
            username = testUsername,
            password = testPassword
        )

        val loginResult = mockMvc.perform(
            post("/api/v0/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )

        var refreshToken = ""
        loginResult
            .andExpect(status().`is`(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andDo {
                refreshToken = objectMapper
                    .readValue(it.response.contentAsString, LoginResponse::class.java)
                    .refreshToken
            }

        val userRequest = RefreshRequest(refreshToken = refreshToken)

//        when
        Thread.sleep(refreshExpirationTime)

        val result = mockMvc.perform(
            post("/api/v0/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
        )

//        then
        result
            .andExpect(status().`is`(401))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.statusCode", `is`(401)))
            .andExpect(jsonPath("$.message", `is`("Invalid refresh token")))
    }
}