package rhino10001.todolist.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.web.bind.annotation.*
import rhino10001.todolist.dto.request.*
import rhino10001.todolist.dto.response.ChangePasswordResponse
import rhino10001.todolist.dto.response.LoginResponse
import rhino10001.todolist.dto.response.RefreshResponse
import rhino10001.todolist.dto.response.RegistrationResponse
import rhino10001.todolist.dto.toRegistrationResponse
import rhino10001.todolist.exception.LoginException
import rhino10001.todolist.exception.RegistrationException
import rhino10001.todolist.service.UserService

@RestController
@RequestMapping("/api/v0/auth")
class AuthenticationController @Autowired constructor(
    val userService: UserService
) {

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody registrationRequest: RegistrationRequest): RegistrationResponse {
        try {
            return userService.register(registrationRequest.toUserDTO()).toRegistrationResponse()
        } catch (ex: Exception) {
            val message = when (ex) {
                is DataIntegrityViolationException -> "User with such username is already registered"
                else -> "Unknown registration error"
            }
            throw RegistrationException(message, ex)
        }
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        try {
            return userService.login(loginRequest.username, loginRequest.password)
        } catch (ex: Exception) {
            val message: String = when (ex) {
                is InternalAuthenticationServiceException -> "Incorrect username"
                is BadCredentialsException -> "Incorrect password"
                else -> "Unknown logging in error"
            }
            throw LoginException(message, ex)
        }
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refresh(@RequestBody refreshRequest: RefreshRequest): RefreshResponse {
        return userService.refresh(refreshRequest.refreshToken)
    }

    @PatchMapping("/change-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun changePassword(
        @RequestBody changePasswordRequest: ChangePasswordRequest,
        request: HttpServletRequest
    ): ChangePasswordResponse {

        val authHeader = request.getHeader("Authorization")
        val prefix = "Bearer "
        val token = authHeader.substringAfter(prefix)

        return userService.changePassword(
            accessToken = token,
            oldPassword = changePasswordRequest.oldPassword,
            newPassword = changePasswordRequest.newPassword,
            newPasswordConfirmation = changePasswordRequest.newPasswordConfirmation
        )
    }
}