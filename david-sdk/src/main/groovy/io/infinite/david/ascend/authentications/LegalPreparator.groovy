package io.infinite.david.ascend.authentications


import io.infinite.ascend.granting.client.authentication.AuthenticationPreparator
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.david.telegram.DavidThread
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Service
class LegalPreparator implements AuthenticationPreparator {

    @Value('${orbitUrl}')
    String orbitUrl

    @Override
    void prepareAuthentication(Map<String, String> publicCredentials, Map<String, String> privateCredentials, Optional<String> prerequisiteJwt) {
        DavidThread davidThread = Thread.currentThread() as DavidThread
        davidThread.send("Welcome to David Chat Bot.")
        davidThread.sleep(600)
        davidThread.send("Please review and our Privacy Policy:")
        davidThread.sleep(600)
        davidThread.send("http://i-t.io/#/PrivacyPolicy")
        davidThread.sleep(600)
        davidThread.confirm("Please confirm that you accept the Privacy Policy:")
    }

}
