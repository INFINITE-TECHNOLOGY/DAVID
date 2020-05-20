package io.infinite.david.ascend.authentications


import io.infinite.ascend.granting.client.authentication.AuthenticationPreparator
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.david.telegram.DavidThread
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Service
class EmailOtpPreparator implements AuthenticationPreparator {

    @Override
    void prepareAuthentication(Map<String, String> publicCredentials, Map<String, String> privateCredentials, Optional<String> prerequisiteJwt) {
        DavidThread davidThread = Thread.currentThread() as DavidThread
        davidThread.silentSender.send("Email authentication", davidThread.chatId)
    }

}
