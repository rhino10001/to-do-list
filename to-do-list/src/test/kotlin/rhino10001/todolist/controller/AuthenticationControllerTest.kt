package rhino10001.todolist.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import rhino10001.todolist.configuration.SpringSecurityConfiguration
import rhino10001.todolist.dto.RoleDTO
import rhino10001.todolist.dto.UserDTO
import rhino10001.todolist.dto.request.*
import rhino10001.todolist.dto.response.*
import rhino10001.todolist.exception.JwtAuthenticationException
import rhino10001.todolist.security.JwtTokenProvider
import rhino10001.todolist.service.UserService

@WebMvcTest(AuthenticationController::class)
@Import(SpringSecurityConfiguration::class)
class AuthenticationControllerTest @Autowired constructor(

    @MockBean
    private val userService: UserService,

    @MockBean
    private val jwtTokenProvider: JwtTokenProvider,

    private val mockMvc: MockMvc,

    private val objectMapper: ObjectMapper
) {

    @Test
    @WithAnonymousUser
    fun givenNewUser_whenRegister_thenReturnsCreated() {

//        given
        val userRequest = RegistrationRequest(
            username = "test_new_username",
            password = "test_password"
        )

//        when
        val createdUserDTO = UserDTO(
            id = 1,
            username = userRequest.username,
            password = userRequest.password,
            roles = listOf(RoleDTO(id = 1))
        )
        `when`(userService.register(userRequest.toUserDTO())).thenReturn(createdUserDTO)

        val result = mockMvc
            .post("/api/v0/auth/registration") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

        //        then
        val expectedResponse = RegistrationResponse(
            id = 1,
            username = userRequest.username
        )

        result
            .andExpect {
                status { `is`(201) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenExistingUser_whenRegister_thenReturnsError422() {

//        given
        val userRequest = RegistrationRequest(
            username = "test_existing_username",
            password = "test_password"
        )

//        when
        val exception = DataIntegrityViolationException("Indifferently")
        `when`(userService.register(userRequest.toUserDTO())).thenThrow(exception)

        val result = mockMvc
            .post("/api/v0/auth/registration") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        val expectedResponse = ExceptionResponse(
            statusCode = 422,
            message = "User with such username is already registered"
        )

        result
            .andExpect {
                status { `is`(422) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenUserWithCorrectData_whenLogin_thenSucceed() {

//        given
        val userRequest = LoginRequest(
            username = "test_username",
            password = "test_password"
        )

//        when
        val accessToken = "test_access_token"
        val refreshToken = "test_refresh_token"

        `when`(userService.login(userRequest.username, userRequest.password))
            .thenReturn(
                LoginResponse(
                    username = userRequest.username,
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )

        val result = mockMvc
            .post("/api/v0/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        val expectedResponse = LoginResponse(
            username = userRequest.username,
            accessToken = accessToken,
            refreshToken = refreshToken
        )

        result
            .andExpect {
                status { `is`(200) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenUserWithUnknownUsername_whenLogin_thenReturnsError401() {

//        given
        val userRequest = LoginRequest(
            username = "test_unknown_username",
            password = "test_password"
        )

//        when
        val exception = InternalAuthenticationServiceException("Indifferently")
        `when`(userService.login(userRequest.username, userRequest.password)).thenThrow(exception)

        val result = mockMvc
            .post("/api/v0/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        val expectedResponse = ExceptionResponse(
            statusCode = 401,
            message = "Incorrect username"
        )

        result
            .andExpect {
                status { `is`(401) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenUserWithIncorrectPassword_whenLogin_thenReturnsError401() {

//        given
        val userRequest = LoginRequest(
            username = "test_username",
            password = "test_incorrect_password"
        )

//        when
        val exception = BadCredentialsException("Indifferently")
        `when`(userService.login(userRequest.username, userRequest.password)).thenThrow(exception)

        val result = mockMvc
            .post("/api/v0/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        val expectedResponse = ExceptionResponse(
            statusCode = 401,
            message = "Incorrect password"
        )

        result
            .andExpect {
                status { `is`(401) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenValidRefreshToken_whenRefresh_thenReturnsSucceed() {

//        given
        val userRequest = RefreshRequest(refreshToken = "test_valid_refresh_token")

//        when
        val newAccessToken = "test_new_access_token"
        val newRefreshToken = "test_new_refresh_token"

        `when`(userService.refresh(userRequest.refreshToken))
            .thenReturn(
                RefreshResponse(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken
                )
            )
        val result = mockMvc
            .post("/api/v0/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        val expectedResponse = RefreshResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        result
            .andExpect {
                status { `is`(200) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @WithAnonymousUser
    fun givenInvalidRefreshToken_whenRefresh_thenReturnsError401() {

//        given
        val userRequest = RefreshRequest(refreshToken = "test_invalid_refresh_token")

//        when
        val exception = JwtAuthenticationException("Invalid refresh token")
        `when`(userService.refresh(userRequest.refreshToken))
            .thenThrow(exception)

        val result = mockMvc
            .post("/api/v0/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        val expectedResponse = ExceptionResponse(
            statusCode = 401,
            message = "Invalid refresh token"
        )

        result
            .andExpect {
                status { `is`(401) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }

    @Test
    @Disabled
    @WithMockUser
    fun givenUserWithCorrectRequest_whenChangePassword_thenReturnsSucceed() {

//        given
        val userRequest = ChangePasswordRequest(
            oldPassword = "oldPassword",
            newPassword = "newPassword",
            newPasswordConfirmation = "newPassword"
        )

//        when
        val result = mockMvc
            .patch("/api/v0/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(userRequest)
            }

//        then
        val expectedResponse = ChangePasswordResponse(message = "Password was successfully changed")

        result
            .andExpect {
                status { `is`(202) }
                content { string(objectMapper.writeValueAsString(expectedResponse)) }
            }
    }
}