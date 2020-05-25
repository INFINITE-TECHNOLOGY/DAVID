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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@BlackBox(level = CarburetorLevel.METHOD)
@Service
@Slf4j
@CompileDynamic
class SmsOtpPreparator implements AuthenticationPreparator {

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
        davidThread.oneTimeKeyboard("What phone should I use?", [
                [new KeyboardButton("My current phone").setRequestContact(true)] as KeyboardRow,
                [new KeyboardButton("Use another phone")] as KeyboardRow,
                [new KeyboardButton("Cancel")] as KeyboardRow
        ])
        String telephone = davidThread.waitForInput(30)
        if (telephone == "Cancel") {
            throw new DavidUserException("Authentication cancelled as per your request.")
        }
        if (telephone == "Use another phone") {
            davidThread.silentSender.send("Please enter your phone:", davidThread.chatId)
            telephone = davidThread.waitForInput(30)
        }
        telephone = telephone.replace("+", "").replace("(", "").replace(")", "").replace(" ", "")
        if (!telephone.matches("\\d{5,15}")) {
            davidThread.silentSender.send("Sorry, it seems like a wrong phone, please try again:", davidThread.chatId)
            telephone = davidThread.waitForInput(30)
        }
        telephone = telephone.replace("+", "").replace("(", "").replace(")", "").replace(" ", "")
        if (!telephone.matches("\\d{5,15}")) {
            throw new DavidUserException("Sorry, it seems like a wrong phone.")
        }
        def managedOtpHandle
        try {
            managedOtpHandle = jsonSlurper.parseText(senderDefaultHttps.expectStatus(
                    new HttpRequest(
                            url: "$orbitUrl/orbit/secured/sendOtpSms",
                            method: "POST",
                            headers: [
                                    "Content-Type" : "application/json",
                                    "Accept"       : "application/json",
                                    "Authorization": "Bearer ${prerequisiteJwt.get()}"
                            ],
                            body: """{
	"telephone": "+$telephone",
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
            throw new DavidUserException("Sorry, but there was a problem sending OTP to this phone.")
        }
        davidThread.silentSender.send("I have sent an OTP to your phone *${telephone[-4..-1]}", davidThread.chatId)
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
        publicCredentials.put("phone", telephone)
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

}
