package io.infinite.david.ascend.selectors

import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.granting.client.services.selectors.AuthorizationSelector
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.david.telegram.DavidException
import io.infinite.david.telegram.DavidThread
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Service
@Primary
@BlackBox(level = CarburetorLevel.METHOD)
class DavidAuthorizationSelector implements AuthorizationSelector {

    @Override
    Authorization select(Set<Authorization> authorizations) {
        DavidThread davidThread = Thread.currentThread() as DavidThread
        return selectWithMessage(authorizations, "Please choose existing authorization:")
    }

    @Override
    Authorization selectPrerequisite(Set<Authorization> Authorizations) {
        return selectWithMessage(Authorizations, "Please choose existing prerequisite authorization:")
    }

    Authorization selectWithMessage(Set<Authorization> authorizations, String message) {
        if (authorizations.size() == 1) {
            return authorizations.first()
        }
        DavidThread davidThread = Thread.currentThread() as DavidThread
        davidThread.messageSender.execute(new SendMessage()
                .setChatId(davidThread.chatId)
                .setText(message)
                .setReplyMarkup(new InlineKeyboardMarkup(
                        keyboard: authorizations.collect { authorization ->
                            [new InlineKeyboardButton(authorization.name).setCallbackData(authorization.name)]
                        }
                )))
        String chosenAuthorizationName = davidThread.waitForInput(30)
        if (!authorizations.collect { it.name }.contains(chosenAuthorizationName)) {
            davidThread.silentSender.send("Sorry, please select one of the above authorizations.", davidThread.chatId)
            chosenAuthorizationName = davidThread.waitForInput(30)
        }
        if (!authorizations.collect { it.name }.contains(chosenAuthorizationName)) {
            throw new DavidException("Authorization choosing failure")
        }
        return authorizations.find { it.name == chosenAuthorizationName }
    }

}
