package rhino10001.todolist.repository

import org.springframework.data.jpa.repository.JpaRepository
import rhino10001.todolist.model.UserEntity

interface UserRepository: JpaRepository<UserEntity, Long> {

    fun findByUsername(username: String): UserEntity
}