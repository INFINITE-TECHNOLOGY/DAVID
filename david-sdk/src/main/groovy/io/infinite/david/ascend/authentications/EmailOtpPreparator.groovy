package io.infinite.david.ascend.authentications

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.client.authentication.AuthenticationPreparator
import io.infinite.ascend.granting.client.services.ClientAuthorizationGrantingService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.david.telegram.DavidThread
import io.infinite.david.telegram.DavidUserException
import io.infinite.http.HttpException
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Service
@Slf4j
@CompileDynamic
class EmailOtpPreparator implements AuthenticationPreparator {

    JsonSlurper jsonSlurper = new JsonSlurper()

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Autowired
    ClientAuthorizationGrantingService clientAuthorizationGrantingService

    @Value('${ascendGrantingUrl}')
    String ascendGrantingUrl

    @Value('${ascendClientPublicKeyName}')
    String ascendClientPublicKeyName

    @Value('${orbitUrl}')
    String orbitUrl

    @Override
    void prepareAuthentication(Map<String, String> publicCredentials, Map<String, String> privateCredentials, Optional<String> prerequisiteJwt) {
        DavidThread davidThread = Thread.currentThread() as DavidThread
        davidThread.silentSender.send("An OTP Authentication needs to be performed to proceed further.", davidThread.chatId)
        Thread.sleep(1000)
        davidThread.silentSender.send("Please enter your email:", davidThread.chatId)
        String email = davidThread.waitForInput(30)
        if (!email.matches(emailRegex)) {
            davidThread.silentSender.send("Sorry, it seems like a wrong email, please try again:", davidThread.chatId)
            email = davidThread.waitForInput(30)
        }
        if (!email.matches(emailRegex)) {
            throw new DavidUserException("Sorry, it seems like a wrong email.")
        }
        def managedOtpHandle
        try {
            managedOtpHandle = jsonSlurper.parseText(senderDefaultHttps.expectStatus(
                    new HttpRequest(
                            url: "$orbitUrl/orbit/secured/sendOtpEmail",
                            method: "POST",
                            headers: [
                                    "Content-Type" : "application/json",
                                    "Accept"       : "application/json",
                                    "Authorization": "Bearer ${prerequisiteJwt.get()}"
                            ],
                            body: """{
	"email": "+$email",
	"templateValues": {
		"action": "Registration"
	},
	"templateSelectionData": {
		"templateName": "OTP",
		"language": "eng"
	}
}"""
                    ),
                    200
            ).body)
        } catch (HttpException httpException) {
            log.warn("OTP sending exception", httpException)
            throw new DavidUserException("Sorry, but there was a problem sending OTP to this email.")
        }
        davidThread.silentSender.send("I have sent an OTP to your email *${email[-4..-1]}", davidThread.chatId)
        davidThread.silentSender.send("Please enter it here:", davidThread.chatId)
        String userOtp = davidThread.waitForInput(120)
        Integer status = validateOtp(managedOtpHandle.guid as String, userOtp)
        if (status != 200) {
            davidThread.silentSender.send("Sorry, the OTP got declined. Please re-enter:", davidThread.chatId)
            userOtp = davidThread.waitForInput(120)
            status = validateOtp(managedOtpHandle.guid as String, userOtp)
            if (status != 200) {
                throw new DavidUserException("Sorry, but there was a problem validating the OTP.")
            }
        }
        publicCredentials.put("otpGuid", managedOtpHandle.guid as String)
        publicCredentials.put("email", email)
        privateCredentials.put("otp", userOtp)
    }

    Integer validateOtp(String guid, String userOtp) {
        return senderDefaultHttps.sendHttpMessage(
                new HttpRequest(
                        url: "$orbitUrl/orbit/public/validateOtp",
                        method: "POST",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ],
                        body: """{
	"guid": "$guid",
	"otp": "$userOtp"
}"""
                )
        ).status
    }

    String emailRegex = """(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"""

}
