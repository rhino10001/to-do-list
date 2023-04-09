package rhino10001.todolist.dto.response

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)