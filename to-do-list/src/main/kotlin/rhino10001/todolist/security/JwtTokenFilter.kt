package rhino10001.todolist.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

class JwtTokenFilter(
    private val jwtUtils: JwtUtils
): GenericFilterBean() {

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val token = jwtUtils.extractToken(request = request as HttpServletRequest)
        if (jwtUtils.validateToken(token)) {
            SecurityContextHolder.getContext().authentication = jwtUtils.getAuthentication(token)
        }
        chain?.doFilter(request, response)
    }
}