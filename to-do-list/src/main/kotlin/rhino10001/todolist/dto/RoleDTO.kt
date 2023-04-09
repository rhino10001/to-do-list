package rhino10001.todolist.dto

import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.model.UserEntity

data class RoleDTO(
    val id: Long = 0,
    val type: RoleEntity.Type = RoleEntity.Type.USER,
    val users: List<UserEntity> = listOf()
)

fun RoleDTO.toEntity() = RoleEntity(
    id = id,
    type = type
)