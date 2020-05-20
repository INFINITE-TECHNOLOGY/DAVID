package io.infinite.david.telegram

import org.springframework.core.NestedRuntimeException

class DavidInterruptedException extends NestedRuntimeException {

    public String message

    DavidInterruptedException(String message) {
        super(message)
    }

    DavidInterruptedException(String message, Throwable cause) {
        super(message, cause)
    }

}
