package rhino10001.todolist.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import rhino10001.todolist.dto.user.UserRequestRegistration
import rhino10001.todolist.dto.user.toDTO
import rhino10001.todolist.dto.user.toResponseRegistration
import rhino10001.todolist.service.UserService

@RestController
class UsersController @Autowired constructor(
    val userService: UserService
) {
    @GetMapping("/hello")
    fun hello() = "Hello world!"

    @GetMapping("/registration")
    fun register() = "RegistrationPage!!"

    @PostMapping("/registration")
    fun register(@RequestBody new: UserRequestRegistration): UserRequestRegistration {
        return userService.register(new.toDTO()).toResponseRegistration()
    }
}