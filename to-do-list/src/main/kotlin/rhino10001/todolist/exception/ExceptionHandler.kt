package rhino10001.todolist.exception

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import rhino10001.todolist.dto.response.ExceptionResponse


@ControllerAdvice
class ExceptionHandler(
    val objectMapper: ObjectMapper
) : ResponseEntityExceptionHandler(), AuthenticationEntryPoint, AccessDeniedHandler {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        val body = objectMapper.writeValueAsString(
            ExceptionResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized"
            )
        )
        response?.setHeader("Content-Type", "application/json;charset=UTF-8")
        response?.writer?.write(body)
        response?.status = HttpStatus.UNAUTHORIZED.value()
    }

    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: AccessDeniedException?
    ) {
        val body = objectMapper.writeValueAsString(
            ExceptionResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden"
            )
        )
        response?.setHeader("Content-Type", "application/json;charset=UTF-8")
        response?.writer?.write(body)
        response?.status = HttpStatus.FORBIDDEN.value()
    }

    @ExceptionHandler(value = [(RegistrationException::class)])
    protected fun onRegistrationException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        return handleExceptionInternal(
            ex,
            ex.message?.let { ExceptionResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), it) },
            HttpHeaders(),
            HttpStatus.UNPROCESSABLE_ENTITY,
            request
        )
    }

    @ExceptionHandler(value = [(LoginException::class)])
    protected fun onLoginException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        return handleExceptionInternal(
            ex,
            ex.message?.let { ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), it) },
            HttpHeaders(),
            HttpStatus.UNAUTHORIZED,
            request
        )
    }

    @ExceptionHandler(value = [(JwtAuthenticationException::class)])
    protected fun onJwtAuthenticationException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        return handleExceptionInternal(
            ex,
            ex.message?.let { ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), it) },
            HttpHeaders(),
            HttpStatus.UNAUTHORIZED,
            request
        )
    }
}