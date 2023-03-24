package rhino10001.todolist.service

import rhino10001.todolist.dto.user.UserDTO

interface UserService {

    fun register(user: UserDTO): UserDTO
    fun findByUsername(username: String): UserDTO
}