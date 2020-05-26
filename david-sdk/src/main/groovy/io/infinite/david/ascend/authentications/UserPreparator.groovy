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
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Service
@Slf4j
@CompileDynamic
class UserPreparator implements AuthenticationPreparator {

    @Autowired
    ClientAuthorizationGrantingService clientAuthorizationGrantingService

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    JsonSlurper jsonSlurper = new JsonSlurper()

    @Value('${ascendGrantingUrl}')
    String ascendGrantingUrl

    @Value('${orbitUrl}')
    String orbitUrl

    @Value('${ascendClientPublicKeyName}')
    String ascendClientPublicKeyName

    @Override
    void prepareAuthentication(Map<String, String> publicCredentials, Map<String, String> privateCredentials, Optional<String> prerequisiteJwt) {
        DavidThread davidThread = Thread.currentThread() as DavidThread
        String phone = publicCredentials.get("phone")
        HttpResponse httpResponse = senderDefaultHttps.sendHttpMessage(
                new HttpRequest(
                        url: "$orbitUrl/orbit/secured/user/$phone",
                        method: "GET",
                        headers: [
                                "Content-Type" : "application/json",
                                "Accept"       : "application/json",
                                "Authorization": "Bearer ${prerequisiteJwt.get()}"
                        ]
                )
        )
        String userGuid
        if (httpResponse.status == 404) {
            davidThread.send("We are going to create a new user account.")
            davidThread.sleep(600)
            davidThread.send("Please review and accept our Terms and Conditions:")
            davidThread.send("http://i-t.io/website/#/TermsAndConditions")
            davidThread.sleep(600)
            davidThread.confirm("Please confirm that you accept the Terms and Conditions:")
            try {
                userGuid = jsonSlurper.parseText(senderDefaultHttps.expectStatus(
                        new HttpRequest(
                                url: "$orbitUrl/orbit/secured/user/$phone",
                                method: "POST",
                                headers: [
                                        "Content-Type" : "application/json",
                                        "Accept"       : "application/json",
                                        "Authorization": "Bearer ${prerequisiteJwt.get()}"
                                ]
                        ), 200
                ).body).guid
            } catch (HttpException exception) {
                log.warn("User API exception", exception)
                throw new DavidUserException("Sorry, there was a problem creating account.")
            }
        } else if (httpResponse.status == 200) {
            davidThread.send("Welcome back!")
            userGuid = jsonSlurper.parseText(httpResponse.body).guid
        } else {
            throw new DavidUserException("Sorry, there was a problem logging in.")
        }
        publicCredentials.put("userGuid", userGuid)
    }

}
