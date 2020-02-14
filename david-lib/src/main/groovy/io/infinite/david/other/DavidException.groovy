package io.infinite.david.other

class DavidException extends Exception {

    Boolean showToUser = true

    DavidException(String message) {
        super(message)
    }

    DavidException(String message, Boolean showToUser) {
        super(message)
        this.showToUser = showToUser
    }

}
