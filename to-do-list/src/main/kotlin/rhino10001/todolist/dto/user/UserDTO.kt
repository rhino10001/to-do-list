package rhino10001.todolist.dto.user

import rhino10001.todolist.dto.role.RoleDTO
import rhino10001.todolist.dto.role.toEntity
import rhino10001.todolist.model.UserEntity
import rhino10001.todolist.security.JwtUserDetailsImpl

data class UserDTO(
    val id: Long = 0,
    val username: String = "",
    val password: String = "",
    val roles: List<RoleDTO> = listOf()
)

fun UserDTO.toEntity() = UserEntity(
    username = username,
    password = password,
    roles = roles.map { it.toEntity() }
)

fun UserDTO.toResponseRegistration() = UserRequestRegistration(
    username = username,
    password = password
)

fun UserDTO.toJwtUserDetailsImpl() = JwtUserDetailsImpl(
    id = id,
    username = username,
    password = password,
    roles = roles
)
