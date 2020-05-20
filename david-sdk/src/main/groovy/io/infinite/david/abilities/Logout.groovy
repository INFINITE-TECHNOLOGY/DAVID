package io.infinite.david.abilities

import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.repositories.RefreshRepository
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
class Logout extends DavidThread {

    @Autowired
    ClientAuthorizationGrantingService clientAuthorizationGrantingService

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    RefreshRepository refreshRepository

    @Value('${ascendGrantingUrl}')
    String ascendGrantingUrl

    Logout(String userName, SilentSender silentSender, MessageSender messageSender, Long chatId, List<String> parameters, ApplicationContext applicationContext) {
        super(userName, silentSender, messageSender, chatId, parameters, applicationContext)
    }

    @Override
    void applyPlugin() {
        Set<Authorization> chatAuthorizations = authorizationRepository.findByClientNamespace(chatId.toString())
        if (chatAuthorizations.empty) {
            showMenu("Sorry, you are currently not logged in.")
            return
        }
        confirm("Are you sure to log out?")
        authorizationRepository.deleteAll(chatAuthorizations)
        refreshRepository.deleteAll(refreshRepository.findByClientNamespace(chatId.toString()))
        authorizationRepository.flush()
        messageSender.execute(new SendMessage()
                .setChatId(chatId)
                .setText("Log out successful.")
                .setReplyMarkup(new InlineKeyboardMarkup(
                        keyboard: [
                                [new InlineKeyboardButton("Menu").setCallbackData("/start")]
                        ]
                )))
    }

}