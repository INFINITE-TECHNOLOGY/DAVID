package io.infinite.david.ascend.selectors

import io.infinite.ascend.granting.client.services.selectors.PrototypeAuthorizationSelector
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
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
class DavidPrototypeAuthorizationSelector implements PrototypeAuthorizationSelector {

    @Override
    PrototypeAuthorization select(Set<PrototypeAuthorization> prototypeAuthorizations) {
        DavidThread davidThread = Thread.currentThread() as DavidThread
        return selectWithMessage(prototypeAuthorizations, "Please choose preferred authorization:")
    }

    @Override
    PrototypeAuthorization selectPrerequisite(Set<PrototypeAuthorization> prototypeAuthorizations) {
        return selectWithMessage(prototypeAuthorizations, "Please choose preferred prerequisite authorization:")
    }

    PrototypeAuthorization selectWithMessage(Set<PrototypeAuthorization> prototypeAuthorizations, String message) {
        if (prototypeAuthorizations.size() == 1) {
            return prototypeAuthorizations.first()
        }
        DavidThread davidThread = Thread.currentThread() as DavidThread
        davidThread.messageSender.execute(new SendMessage()
                .setChatId(davidThread.chatId)
                .setText(message)
                .setReplyMarkup(new InlineKeyboardMarkup(
                        keyboard: prototypeAuthorizations.collect { prototypeAuthorization ->
                            [new InlineKeyboardButton(prototypeAuthorization.name).setCallbackData(prototypeAuthorization.name)]
                        }
                )))
        String chosenAuthorizationName = davidThread.waitForInput(30)
        if (!prototypeAuthorizations.collect { it.name }.contains(chosenAuthorizationName)) {
            davidThread.silentSender.send("Sorry, please select one of the above authorizations.", davidThread.chatId)
            chosenAuthorizationName = davidThread.waitForInput(30)
        }
        if (!prototypeAuthorizations.collect { it.name }.contains(chosenAuthorizationName)) {
            throw new DavidException("PrototypeAuthorization choosing failure")
        }
        return prototypeAuthorizations.find { it.name == chosenAuthorizationName }
    }

}