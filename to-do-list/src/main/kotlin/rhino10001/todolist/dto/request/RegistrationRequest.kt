package rhino10001.todolist.dto.request

import rhino10001.todolist.dto.UserDTO

data class RegistrationRequest(
    val username: String,
    val password: String
)

fun RegistrationRequest.toUserDTO() = UserDTO(
    username = username,
    password = password
)
