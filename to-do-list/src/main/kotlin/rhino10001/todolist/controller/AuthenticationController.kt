package rhino10001.todolist.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
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
import rhino10001.todolist.security.JwtAuthenticationException
import rhino10001.todolist.security.JwtUtils
import rhino10001.todolist.service.UserService

@RestController
@RequestMapping("/api/v0/auth")
class AuthenticationController @Autowired constructor(
    val userService: UserService,
    val authenticationManager: AuthenticationManager,
    val jwtUtils: JwtUtils
) {

    @PostMapping("/registration")
    fun register(@RequestBody registrationRequest: RegistrationRequest): RegistrationResponse {
        return userService.register(registrationRequest.toUserDTO()).toRegistrationResponse()
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
        val userDTO = userService.findByUsername(loginRequest.username)
        return LoginResponse(
            username = userDTO.username,
            accessToken = jwtUtils.generateAccessToken(userDTO.username, userDTO.roles),
            refreshToken = jwtUtils.generateRefreshToken(userDTO.username)
        )
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody refreshRequest: RefreshRequest): RefreshResponse {
        val token = refreshRequest.refreshToken
        if (!jwtUtils.validateRefreshToken(token)) {
            throw JwtAuthenticationException("Invalid Token")
        }
        val userDTO = userService.findByUsername(jwtUtils.getUsernameFromRefreshToken(token))
        return RefreshResponse(
            accessToken = jwtUtils.generateAccessToken(userDTO.username, userDTO.roles),
            refreshToken = jwtUtils.generateRefreshToken(userDTO.username)
        )
    }
}