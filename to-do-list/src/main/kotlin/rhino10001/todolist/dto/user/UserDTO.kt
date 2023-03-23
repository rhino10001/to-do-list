package rhino10001.todolist.dto.user

import rhino10001.todolist.dto.role.RoleDTO

data class UserDTO(
    val id: Long = 0,
    val username: String = "",
    val password: String = "",
    val roles: List<RoleDTO> = mutableListOf()
)
