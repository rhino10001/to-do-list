package rhino10001.todolist.dto.request

import rhino10001.todolist.dto.UserDTO

data class LoginRequest(
    val username: String,
    val password: String
)

fun LoginRequest.toUserDTO() = UserDTO(
    username = username,
    password = password
)
