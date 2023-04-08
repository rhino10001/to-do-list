package rhino10001.todolist.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rhino10001.todolist.dto.user.UserRequestAuthentication
import rhino10001.todolist.dto.user.UserRequestRegistration
import rhino10001.todolist.dto.user.toDTO
import rhino10001.todolist.dto.user.toResponseRegistration
import rhino10001.todolist.security.JwtUtils
import rhino10001.todolist.service.UserService

@RestController
@RequestMapping("/api/v0")
class UsersController @Autowired constructor(
    val userService: UserService,
    val authenticationManager: AuthenticationManager,
    val jwtUtils: JwtUtils
) {
    @GetMapping("/hello")
    fun hello() = "Hello world!"

    @GetMapping("/registration")
    fun register() = "RegistrationPage!!"

    @PostMapping("/registration")
    fun register(@RequestBody new: UserRequestRegistration): UserRequestRegistration {
        return userService.register(new.toDTO()).toResponseRegistration()
    }

    @PostMapping("/login")
    fun login(@RequestBody user: UserRequestAuthentication): ResponseEntity<Any> {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(user.username, user.password))
        val userDTO = userService.findByUsername(user.username)
        val token = jwtUtils.generateAuthenticationToken(userDTO.username, userDTO.roles)
        val response = mapOf("username" to userDTO.username, "token" to token)
        return ResponseEntity.ok(response)
    }
}