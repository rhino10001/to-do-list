package rhino10001.todolist.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

class JwtTokenFilter(
    private val tokenProvider: JwtTokenProvider
) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val token = extractToken(request = request as HttpServletRequest)
        if (tokenProvider.validateAccessToken(token)) {
            SecurityContextHolder.getContext().authentication = tokenProvider.getAuthentication(token)
        }
        chain?.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String {
        val authHeader = request.getHeader("Authorization")
        val prefix = "Bearer "
        return if (authHeader != null && authHeader.startsWith(prefix)) authHeader.substring(prefix.length) else ""
    }
}