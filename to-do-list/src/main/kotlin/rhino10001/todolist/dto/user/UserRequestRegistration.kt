package rhino10001.todolist.dto.user

data class UserRequestRegistration(
    val username: String,
    val password: String
)

fun UserRequestRegistration.toDTO() = UserDTO(
    username = username,
    password = password
)
