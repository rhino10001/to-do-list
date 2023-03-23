package rhino10001.todolist.repository

import org.springframework.data.jpa.repository.JpaRepository
import rhino10001.todolist.model.RoleEntity

interface RoleRepository: JpaRepository<RoleEntity, Long> {

    fun findByType(type: RoleEntity.Type): RoleEntity
}