package rhino10001.todolist.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import rhino10001.todolist.dto.user.UserDTO
import rhino10001.todolist.dto.user.toEntity
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.model.toDTO
import rhino10001.todolist.repository.RoleRepository
import rhino10001.todolist.repository.UserRepository
import rhino10001.todolist.service.UserService

@Service
class UserServiceImpl @Autowired constructor(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
//    val passwordEncoder: PasswordEncoder
) : UserService {

    override fun register(user: UserDTO): UserDTO {
        val roleUser = roleRepository.findByType(RoleEntity.Type.USER).toDTO()
        val copy = user.copy(
            roles = listOf(roleUser)
        )
        return userRepository.save(copy.toEntity()).toDTO()
    }

    override fun findByUsername(username: String): UserDTO {
        return userRepository.findByUsername(username).toDTO()
    }
}