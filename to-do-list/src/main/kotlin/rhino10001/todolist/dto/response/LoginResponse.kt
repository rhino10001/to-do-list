package rhino10001.todolist.dto.response

data class LoginResponse(
    val username: String,
    val accessToken: String,
    val refreshToken: String
)