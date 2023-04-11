package rhino10001.todolist.exception

class LoginException: RuntimeException {
    constructor(msg: String): super(msg)
    constructor(msg: String, cause: Throwable): super(msg, cause)
}