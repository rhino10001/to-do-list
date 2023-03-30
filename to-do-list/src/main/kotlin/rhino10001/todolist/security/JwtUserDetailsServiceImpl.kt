package rhino10001.todolist.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import rhino10001.todolist.dto.user.toJwtUserDetailsImpl
import rhino10001.todolist.service.UserService

@Service
class JwtUserDetailsServiceImpl @Autowired constructor(val userService: UserService): UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        return userService.findByUsername(username ?: "default").toJwtUserDetailsImpl()
    }
}