package rhino10001.todolist.service

import rhino10001.todolist.dto.UserDTO
import rhino10001.todolist.dto.response.ChangePasswordResponse
import rhino10001.todolist.dto.response.LoginResponse
import rhino10001.todolist.dto.response.RefreshResponse

interface UserService {

    fun register(user: UserDTO): UserDTO

    fun login(username: String, password: String): LoginResponse

    fun refresh(refreshToken: String): RefreshResponse

    fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): ChangePasswordResponse
}