package rhino10001.todolist.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rhino10001.todolist.dto.RoleDTO
import rhino10001.todolist.dto.request.ChangePasswordRequest
import rhino10001.todolist.dto.request.LoginRequest
import rhino10001.todolist.dto.request.RefreshRequest
import rhino10001.todolist.dto.request.RegistrationRequest
import rhino10001.todolist.dto.response.LoginResponse
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.repository.UserRepository
import rhino10001.todolist.security.JwtTokenProvider

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application.yml"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationControllerIntegrationTest @Autowired constructor(

    private val mockMvc: MockMvc,

    private val objectMapper: ObjectMapper,

    private val userRepository: UserRepository,

    private val jwtTokenProvider: JwtTokenProvider
) {

    private val testUsername = "test_username"
    private val testPassword = "test_password"
    private val testRoles: List<RoleDTO> = listOf(RoleDTO(type = RoleEntity.Type.ROLE_USER))

    @BeforeAll
    @WithAnonymousUser
    fun givenNewUser_whenRegister_thenReturnsCreated() {

        try {
            val testUser = userRepository.findByUsername(testUsername)
            userRepository.delete(testUser)
        } catch (ignored: EmptyResultDataAccessException) {
        }

//        given
        val userRequest = RegistrationRequest(
            username = testUsername,
            password = testPassword
        )

//        when
        val result = mockMvc
            .post("/api/v0/auth/registration") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(201) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id", notNullValue(Long::class.java))
                jsonPath("$.username", `is`(userRequest.username))
            }
    }

    @Test
    @WithAnonymousUser
    fun givenExistingUser_whenRegister_thenReturnsError422() {

//        given
        val userRequest = RegistrationRequest(
            username = testUsername,
            password = testPassword
        )

//        when
        val result = mockMvc
            .post("/api/v0/auth/registration") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(422) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.statusCode", `is`(422))
                jsonPath("$.message", `is`("User with such username is already registered"))
            }
    }

    @Test
    @WithAnonymousUser
    fun givenUserWithCorrectData_whenLogin_thenSucceed() {

//        given
        val userRequest = LoginRequest(
            username = testUsername,
            password = testPassword
        )

//        when
        val result = mockMvc
            .post("/api/v0/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(200) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.username", `is`(userRequest.username))
                jsonPath("$.accessToken", notNullValue())
                jsonPath("$.refreshToken", notNullValue())
            }
    }

    @Test
    @WithAnonymousUser
    fun givenUserWithUnknownUsername_whenLogin_thenReturnsError401() {

//        given
        val userRequest = LoginRequest(
            username = "test_unknown_username",
            password = testPassword
        )

//        when
        val result = mockMvc
            .post("/api/v0/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(401) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.statusCode", `is`(401))
                jsonPath("$.message", `is`("Incorrect username"))
            }
    }

    @Test
    @WithAnonymousUser
    fun givenUserWithIncorrectPassword_whenLogin_thenReturnsError401() {

//        given
        val userRequest = LoginRequest(
            username = testUsername,
            password = "test_incorrect_password"
        )

//        when
        val result = mockMvc
            .post("/api/v0/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(401) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.statusCode", `is`(401))
                jsonPath("$.message", `is`("Incorrect password"))
            }
    }

    @Test
    @WithAnonymousUser
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
        val result = mockMvc
            .post("/api/v0/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(200) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.accessToken", notNullValue())
                jsonPath("$.refreshToken", notNullValue())
            }
    }

    @Test
    @WithAnonymousUser
    fun givenInvalidRefreshToken_whenRefresh_thenReturnsError401() {

//        given
        val loginRequest = LoginRequest(
            username = testUsername,
            password = testPassword
        )

        val loginResult = mockMvc
            .post("/api/v0/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(loginRequest)
            }

        var refreshToken = ""
        loginResult
            .andExpect {
                status { `is`(200) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.refreshToken", notNullValue())
            }
            .andDo {
                refreshToken = objectMapper
                    .readValue(loginResult.andReturn().response.contentAsString, LoginResponse::class.java)
                    .refreshToken
            }

        val userRequest = RefreshRequest(refreshToken = refreshToken)

//        when
        Thread.sleep(jwtTokenProvider.refreshExpirationTime)

        val result = mockMvc
            .post("/api/v0/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(401) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.statusCode", `is`(401))
                jsonPath("$.message", `is`("Invalid refresh token"))
            }
    }

    @Test
    @WithMockUser
    fun givenRequestWithCorrectRequest_whenChangePassword_thenReturnsSucceed() {

//        given
        val accessToken = jwtTokenProvider.generateAccessToken(testUsername, testRoles)
        val userRequest = ChangePasswordRequest(
            oldPassword = testPassword,
            newPassword = "new_password",
            newPasswordConfirmation = "new_password"
        )

//        when
        val result = mockMvc
            .patch("/api/v0/auth/change-password") {
                headers {
                    setBearerAuth(accessToken)
                }
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(202) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.message", `is`("Password was successfully changed"))
            }
    }

    @Test
    @WithMockUser
    fun givenRequestWithIncorrectPassword_whenChangePassword_thenReturnsError406() {

//        given
        val accessToken = jwtTokenProvider.generateAccessToken(testUsername, testRoles)
        val userRequest = ChangePasswordRequest(
            oldPassword = "wrong_old_password",
            newPassword = "new_password",
            newPasswordConfirmation = "new_password"
        )

//        when
        val result = mockMvc
            .patch("/api/v0/auth/change-password") {
                headers {
                    setBearerAuth(accessToken)
                }
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(406) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.statusCode", `is`(406))
                jsonPath("$.message", `is`("Incorrect old password"))
            }
    }

    @Test
    @WithMockUser
    fun givenRequestWithDifferentNewPasswordAndConfirmation_whenChangePassword_thenReturnsError406() {

//        given
        val accessToken = jwtTokenProvider.generateAccessToken(testUsername, testRoles)
        val userRequest = ChangePasswordRequest(
            oldPassword = testPassword,
            newPassword = "new_password",
            newPasswordConfirmation = "wrong_new_password"
        )

//        when
        val result = mockMvc
            .patch("/api/v0/auth/change-password") {
                headers {
                    setBearerAuth(accessToken)
                }
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        result
            .andExpect {
                status { `is`(406) }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.statusCode", `is`(406))
                jsonPath("$.message", `is`("New password is not equal to confirmation"))
            }
    }
}