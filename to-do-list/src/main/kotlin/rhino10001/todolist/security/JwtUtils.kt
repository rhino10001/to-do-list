package rhino10001.todolist.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import rhino10001.todolist.dto.role.RoleDTO
import java.util.*

@Component
class JwtUtils @Autowired constructor(
//    @Value("\${jwt.authenticationToken.secret}")
    val secret: String = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111",
//    @Value("\${jwt.authenticationToken.expiration}")
    val expirationTime: Long = 3600000,
    val userDetailsService: UserDetailsService
) {

    fun generateAuthenticationToken(username: String, roles: List<RoleDTO>): String {
        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = roles.map { it.type.name }
        val nowDate = Date()
        val expirationDate = Date(nowDate.time + expirationTime)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(nowDate)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.toByteArray()))
            .compact()
    }

    fun getAuthentication(token: String): Authentication? {
        val userDetails = userDetailsService.loadUserByUsername(getUsername(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    private fun getUsername(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secret.toByteArray())
            .build()
            .parseClaimsJws(token).body.subject
    }

    fun extractToken(request: HttpServletRequest): String {
        val authHeader = request.getHeader("Authorization")
        val prefix = "Bearer "
        return if (authHeader != null && authHeader.startsWith(prefix)) authHeader.substring(prefix.length) else "invalidToken"
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims = Jwts.parserBuilder().setSigningKey(secret.toByteArray()).build().parseClaimsJws(token)
            if (claims.body.expiration.after(Date())) return true
        } catch (e: Exception) {
            when(e) {
                is ExpiredJwtException, is IllegalArgumentException -> throw JwtAuthenticationException("Token not valid")
            }
        }
        return false
    }
}