package io.infinite.david.telegram

import org.springframework.core.NestedRuntimeException

class DavidException extends NestedRuntimeException {

    public String message

    DavidException(String message) {
        super(message)
    }

    DavidException(String message, Throwable cause) {
        super(message, cause)
    }

}
