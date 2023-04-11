package rhino10001.todolist.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import rhino10001.todolist.dto.request.LoginRequest
import rhino10001.todolist.dto.request.RefreshRequest
import rhino10001.todolist.dto.request.RegistrationRequest
import rhino10001.todolist.dto.request.toUserDTO
import rhino10001.todolist.dto.response.LoginResponse
import rhino10001.todolist.dto.response.RefreshResponse
import rhino10001.todolist.dto.response.RegistrationResponse
import rhino10001.todolist.dto.toRegistrationResponse
import rhino10001.todolist.exception.JwtAuthenticationException
import rhino10001.todolist.exception.LoginException
import rhino10001.todolist.exception.RegistrationException
import rhino10001.todolist.security.JwtTokenProvider
import rhino10001.todolist.service.UserService

@RestController
@RequestMapping("/api/v0/auth")
class AuthenticationController @Autowired constructor(
    val userService: UserService,
    val authenticationManager: AuthenticationManager,
    val jwtUtils: JwtTokenProvider
) {

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody registrationRequest: RegistrationRequest): RegistrationResponse {
        try {
            return userService.register(registrationRequest.toUserDTO()).toRegistrationResponse()
        } catch (ex: Exception) {
            val message = when(ex) {
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
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.username,
                    loginRequest.password
                )
            )
            val userDTO = userService.findByUsername(loginRequest.username)
            return LoginResponse(
                username = userDTO.username,
                accessToken = jwtUtils.generateAccessToken(userDTO.username, userDTO.roles),
                refreshToken = jwtUtils.generateRefreshToken(userDTO.username)
            )
        } catch (ex: Exception) {
            val message: String = when(ex) {
                is InternalAuthenticationServiceException -> "Incorrect username"
                is BadCredentialsException -> "Incorrect password"
                else -> "Unknown logging in error"
            }
            throw LoginException(message, ex)
        }
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshRequest: RefreshRequest): RefreshResponse {
        val token = refreshRequest.refreshToken
        if (!jwtUtils.validateRefreshToken(token)) {
            throw JwtAuthenticationException("Invalid refresh token")
        }
        val userDTO = userService.findByUsername(jwtUtils.getUsernameFromRefreshToken(token))
        return RefreshResponse(
            accessToken = jwtUtils.generateAccessToken(userDTO.username, userDTO.roles),
            refreshToken = jwtUtils.generateRefreshToken(userDTO.username)
        )
    }
}