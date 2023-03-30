package rhino10001.todolist.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import rhino10001.todolist.dto.role.RoleDTO

class JwtUserDetailsImpl(
    val id: Long = 0,
    @JvmField val username: String = "",
    @JvmField val password: String = "",
    val roles: List<RoleDTO> = listOf()
): UserDetails {

    override fun getAuthorities() = roles.map { SimpleGrantedAuthority(it.type.name) }.toMutableList()

    override fun getPassword() = password

    override fun getUsername() = username

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}