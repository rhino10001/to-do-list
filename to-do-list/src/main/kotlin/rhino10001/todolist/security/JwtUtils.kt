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
    val accessSecret: String = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111",
//    @Value("\${jwt.authenticationToken.expiration}")
    val accessExpirationTime: Long = 30000,
//    @Value("\${jwt.authenticationToken.secret}")
    val refreshSecret: String = "" +
            "",
//    @Value("\${jwt.authenticationToken.expiration}")
    val refreshExpirationTime: Long = 120000,
    val userDetailsService: UserDetailsService
) {

    fun generateAccessToken(username: String, roles: List<RoleDTO>): String {
        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = roles.map { it.type.name }
        val nowDate = Date()
        val expirationDate = Date(nowDate.time + accessExpirationTime)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(nowDate)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(accessSecret.toByteArray()))
            .compact()
    }

    fun validateAccessToken(token: String): Boolean {
        try {
            val claims = Jwts.parserBuilder().setSigningKey(accessSecret.toByteArray()).build().parseClaimsJws(token)
            if (claims.body.expiration.after(Date())) return true
        } catch (e: Exception) {
            when(e) {
                is ExpiredJwtException, is IllegalArgumentException -> throw JwtAuthenticationException("Token not valid")
            }
        }
        return false
    }

    private fun getUsernameFromAccessToken(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(accessSecret.toByteArray())
            .build()
            .parseClaimsJws(token).body.subject
    }

    fun generateRefreshToken(username: String): String {
        val claims = Jwts.claims().setSubject(username)
        val nowDate = Date()
        val expirationDate = Date(nowDate.time + refreshExpirationTime)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(nowDate)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(refreshSecret.toByteArray()))
            .compact()
    }

    fun validateRefreshToken(token: String): Boolean {
        try {
            val claims = Jwts.parserBuilder().setSigningKey(refreshSecret.toByteArray()).build().parseClaimsJws(token)
            if (claims.body.expiration.after(Date())) return true
        } catch (e: Exception) {
            when(e) {
                is ExpiredJwtException, is IllegalArgumentException -> throw JwtAuthenticationException("Token not valid")
            }
        }
        return false
    }

    fun getUsernameFromRefreshToken(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(refreshSecret.toByteArray())
            .build()
            .parseClaimsJws(token).body.subject
    }

    fun getAuthentication(token: String): Authentication? {
        val userDetails = userDetailsService.loadUserByUsername(getUsernameFromAccessToken(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    fun extractToken(request: HttpServletRequest): String {
        val authHeader = request.getHeader("Authorization")
        val prefix = "Bearer "
        return if (authHeader != null && authHeader.startsWith(prefix)) authHeader.substring(prefix.length) else "invalidToken"
    }
}