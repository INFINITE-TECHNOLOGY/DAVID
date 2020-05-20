package io.infinite.david.abilities

import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.granting.client.services.AuthorizedHttpRequest
import io.infinite.ascend.granting.client.services.ClientAuthorizationGrantingService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.david.telegram.DavidThread
import io.infinite.david.telegram.DavidUserException
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import io.infinite.orbit.model.HistoryRecord
import org.apache.commons.lang3.time.FastDateFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@BlackBox(level = CarburetorLevel.METHOD)
class History extends DavidThread {

    @Autowired
    ClientAuthorizationGrantingService clientAuthorizationGrantingService

    @Autowired
    AuthorizationRepository authorizationRepository

    ObjectMapper objectMapper = new ObjectMapper()

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

    @Value('${orbitUrl}')
    String orbitUrl

    @Value('${ascendGrantingUrl}')
    String ascendGrantingUrl

    Authorization authorization

    History(String userName, SilentSender silentSender, MessageSender messageSender, Long chatId, List<String> parameters, ApplicationContext applicationContext) {
        super(userName, silentSender, messageSender, chatId, parameters, applicationContext)
    }

    @Override
    void applyPlugin() {
        authorization = clientAuthorizationGrantingService.grantByScope("registeredUserScope", ascendGrantingUrl, chatId.toString(), "OrbitSaaS")
        oneTimeKeyboard("How many transactions should I display?", [
                [new KeyboardButton("1")] as KeyboardRow,
                [new KeyboardButton("5")] as KeyboardRow,
                [new KeyboardButton("All")] as KeyboardRow,
                [new KeyboardButton("Cancel")] as KeyboardRow
        ])
        String tranCount = waitForInput(15)
        if (tranCount == "Cancel") {
            throw new DavidUserException("Action cancelled as per your request.")//todo: put to David
        }
        if (tranCount != "All") {
            validateDoubleNumber(tranCount, 0, 100)
        }
        getHistoryRecords(tranCount).each {
            send("${it.amount.toString()} $it.currency on ${fastDateFormat.format(it.date)}")
        }
        showMenu("Return to Menu:")
    }

    Set<HistoryRecord> getHistoryRecords(String tranCount) {
        HttpResponse httpResponse = clientAuthorizationGrantingService.sendAuthorizedHttpMessage(
                new AuthorizedHttpRequest(
                        url: "$orbitUrl/orbit/secured/phone/${authorization.authorizedCredentials.get("phone")}/history${tranCount == "All" ? "" : "?tranCount=${tranCount}"}",
                        method: "GET",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ],
                        scopeName: "registeredUserScope",
                        ascendUrl: ascendGrantingUrl,
                        authorizationClientNamespace: chatId.toString(),
                        authorizationServerNamespace: "OrbitSaaS"
                )
        )
        return objectMapper.readValue(httpResponse.body, HistoryRecord[].class)
    }

}