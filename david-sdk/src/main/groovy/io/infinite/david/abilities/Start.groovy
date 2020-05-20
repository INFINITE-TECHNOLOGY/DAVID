package io.infinite.david.abilities

import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.granting.client.services.ClientAuthorizationGrantingService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.david.telegram.DavidThread
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@BlackBox(level = CarburetorLevel.METHOD)
class Start extends DavidThread {

    @Autowired
    ClientAuthorizationGrantingService clientAuthorizationGrantingService

    @Autowired
    AuthorizationRepository authorizationRepository

    @Value('${ascendGrantingUrl}')
    String ascendGrantingUrl

    Start(String userName, SilentSender silentSender, MessageSender messageSender, Long chatId, List<String> parameters, ApplicationContext applicationContext) {
        super(userName, silentSender, messageSender, chatId, parameters, applicationContext)
    }

    @Override
    void applyPlugin() {
        Authorization userServices = clientAuthorizationGrantingService.grantByScope("registeredUserScope", ascendGrantingUrl, chatId.toString(), "OrbitSaaS")
        messageSender.execute(new SendMessage()
                .setChatId(chatId)
                .setText("Greetings, ${userName}! How may I help you?")
                .setReplyMarkup(new InlineKeyboardMarkup(
                        keyboard: [
                                [new InlineKeyboardButton("Send").setCallbackData("/send")],
                                [new InlineKeyboardButton("Receive").setCallbackData("/receive")],
                                [new InlineKeyboardButton("History").setCallbackData("/history")],
                                [new InlineKeyboardButton("Settings").setCallbackData("/settings")],
                                [new InlineKeyboardButton("Log out").setCallbackData("/logout")],
                                [new InlineKeyboardButton("Menu").setCallbackData("/start")]
                        ]
                )))
    }

}