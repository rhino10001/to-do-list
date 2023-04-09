package rhino10001.todolist.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v0")
class CheckController {

    @GetMapping("/hello")
    fun hello() = "Hello world!"

    @GetMapping("/registration")
    fun register() = "RegistrationPage!!"
}