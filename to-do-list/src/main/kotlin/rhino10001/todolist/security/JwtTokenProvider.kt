package rhino10001.todolist.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import rhino10001.todolist.dto.RoleDTO
import java.util.*

@Component
data class JwtTokenProvider @Autowired constructor(

    @Value("\${jwt.authenticationToken.secret}")
    val accessSecret: String,
    @Value("\${jwt.authenticationToken.expirationTime}")
    val accessExpirationTime: Long,
    @Value("\${jwt.refreshToken.secret}")
    val refreshSecret: String,
    @Value("\${jwt.refreshToken.expirationTime}")
    val refreshExpirationTime: Long,
    val userDetailsService: UserDetailsService
) {

    fun generateAccessToken(username: String, roles: List<RoleDTO>): String {
        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = roles.map { it.type.name }
        return generateToken(claims, accessExpirationTime, accessSecret)
    }

    fun generateRefreshToken(username: String): String {
        val claims = Jwts.claims().setSubject(username)
        return generateToken(claims, refreshExpirationTime, refreshSecret)
    }

    fun validateAccessToken(token: String) = validateToken(token, accessSecret)

    fun validateRefreshToken(token: String) = validateToken(token, refreshSecret)

    fun getUsernameFromAccessToken(token: String) = parseSubject(token, accessSecret)

    fun getUsernameFromRefreshToken(token: String) = parseSubject(token, refreshSecret)

    fun getAuthentication(token: String): Authentication? {
        val userDetails = userDetailsService.loadUserByUsername(getUsernameFromAccessToken(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    private fun parseSubject(token: String, secret: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secret.toByteArray())
            .build()
            .parseClaimsJws(token).body.subject
    }

    private fun generateToken(claims: Claims, expirationTime: Long, secret: String): String {
        val nowDate = Date()
        val expirationDate = Date(nowDate.time + expirationTime)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(nowDate)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.toByteArray()))
            .compact()
    }

    private fun validateToken(token: String, secret: String): Boolean {
        try {
            val claims = Jwts.parserBuilder().setSigningKey(secret.toByteArray()).build().parseClaimsJws(token)
            if (claims.body.expiration.after(Date())) return true
        } catch (_: JwtException) {
        } catch (_: IllegalArgumentException) {
        } catch (_: ClassCastException) {}
        return false
    }
}