package rhino10001.todolist.dto.request

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val newPasswordConfirmation: String
)
