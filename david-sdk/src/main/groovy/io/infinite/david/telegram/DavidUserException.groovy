package io.infinite.david.telegram

import org.springframework.core.NestedRuntimeException

class DavidUserException extends NestedRuntimeException {

    public String message

    DavidUserException(String message) {
        super(message)
    }

}
