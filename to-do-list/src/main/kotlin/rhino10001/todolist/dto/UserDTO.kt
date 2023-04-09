package rhino10001.todolist.dto

import rhino10001.todolist.dto.response.RegistrationResponse
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

fun UserDTO.toRegistrationResponse() = RegistrationResponse(
    id = id,
    username = username
)

fun UserDTO.toJwtUserDetailsImpl() = JwtUserDetailsImpl(
    id = id,
    username = username,
    password = password,
    roles = roles
)
