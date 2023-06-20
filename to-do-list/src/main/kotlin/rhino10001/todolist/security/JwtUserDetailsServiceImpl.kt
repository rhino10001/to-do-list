package rhino10001.todolist.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import rhino10001.todolist.dto.toJwtUserDetailsImpl
import rhino10001.todolist.model.toDTO
import rhino10001.todolist.repository.UserRepository

@Service
class JwtUserDetailsServiceImpl @Autowired constructor(val userRepository: UserRepository): UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        return userRepository.findByUsername(username ?: "default").toDTO().toJwtUserDetailsImpl()
    }
}