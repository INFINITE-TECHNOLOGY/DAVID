package io.infinite.david.ascend.selectors

import io.infinite.ascend.granting.client.services.selectors.PrototypeIdentitySelector
import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
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
class DavidPrototypeIdentitySelector implements PrototypeIdentitySelector {

    @Override
    PrototypeIdentity select(Set<PrototypeIdentity> prototypeIdentities) {
        return selectWithMessage(prototypeIdentities, "Please choose preferred identity:")
    }

    @Override
    PrototypeIdentity selectPrerequisite(Set<PrototypeIdentity> prototypeIdentities) {
        return selectWithMessage(prototypeIdentities, "Please choose preferred identity:")
    }

    PrototypeIdentity selectWithMessage(Set<PrototypeIdentity> prototypeIdentities, String message) {
        if (prototypeIdentities.size() == 1) {
            return prototypeIdentities.first()
        }
        DavidThread davidThread = Thread.currentThread() as DavidThread
        davidThread.messageSender.execute(new SendMessage()
                .setChatId(davidThread.chatId)
                .setText(message)
                .setReplyMarkup(new InlineKeyboardMarkup(
                        keyboard: prototypeIdentities.collect { prototypeIdentity ->
                            [new InlineKeyboardButton(prototypeIdentity.name).setCallbackData(prototypeIdentity.name)]
                        }
                )))
        String chosenIdentityName = davidThread.waitForInput(30)
        if (!prototypeIdentities.collect { it.name }.contains(chosenIdentityName)) {
            davidThread.silentSender.send("Sorry, please select one of the above identities.", davidThread.chatId)
            chosenIdentityName = davidThread.waitForInput(30)
        }
        if (!prototypeIdentities.collect { it.name }.contains(chosenIdentityName)) {
            throw new DavidException("PrototypeIdentity choosing failure")
        }
        return prototypeIdentities.find { it.name == chosenIdentityName }
    }

}
