package rhino10001.todolist.dto.user

data class UserRequestAuthentication(
    val username: String,
    val password: String
)

fun UserRequestAuthentication.toDTO() = UserDTO(
    username = username,
    password = password
)
