package rhino10001.todolist.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import rhino10001.todolist.dto.user.*
import rhino10001.todolist.security.JwtUtils
import rhino10001.todolist.service.UserService

@RestController
@RequestMapping("/api/v0/auth")
class AuthenticationController @Autowired constructor(
    val userService: UserService,
    val authenticationManager: AuthenticationManager,
    val jwtUtils: JwtUtils
) {
    @GetMapping("/hello")
    fun hello() = "Hello world!"

    @GetMapping("/registration")
    fun register() = "RegistrationPage!!"

    @PostMapping("/registration")
    fun register(@RequestBody newUser: UserRequestRegistration): UserRequestRegistration {
        return userService.register(newUser.toDTO()).toResponseRegistration()
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: UserRequestAuthentication): ResponseEntity<Any> {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
        val userDTO = userService.findByUsername(loginRequest.username)
        val accessToken = jwtUtils.generateAccessToken(userDTO.username, userDTO.roles)
        val refreshToken = jwtUtils.generateRefreshToken(userDTO.username)
        val response = mapOf(
            "username" to userDTO.username,
            "accessToken" to accessToken,
            "refreshToken" to refreshToken
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refreshToken")
    fun refreshToken(@RequestBody refreshRequest: RefreshRequest): ResponseEntity<Any> {
        val token = refreshRequest.refreshToken
        if (jwtUtils.validateRefreshToken(token)) {
            val userDTO = userService.findByUsername(jwtUtils.getUsernameFromRefreshToken(token))
            val accessToken = jwtUtils.generateAccessToken(userDTO.username, userDTO.roles)
            val refreshToken = jwtUtils.generateRefreshToken(userDTO.username)
            val response = mapOf(
                "accessToken" to accessToken,
                "refreshToken" to refreshToken
            )
            return ResponseEntity.ok(response)
        }
        return ResponseEntity.status(403).body("Невалидный токен")
    }
}