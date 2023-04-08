package rhino10001.todolist.security

import org.springframework.security.core.AuthenticationException

class JwtAuthenticationException: AuthenticationException {

    constructor(msg: String): super(msg)
    constructor(msg: String, cause: Throwable): super(msg, cause)
}