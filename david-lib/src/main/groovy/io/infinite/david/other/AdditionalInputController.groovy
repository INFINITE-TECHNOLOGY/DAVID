package io.infinite.david.other

import io.infinite.blackbox.BlackBox
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@BlackBox
class AdditionalInputController {

    static String waitForInput(DavidThread davidThread, Integer timeoutSeconds, Long chatId, MessageSender messageSender) {
        synchronized (davidThread) {
            davidThread.isWaitingForInput = true
            davidThread.wait(timeoutSeconds * 1000)
            if (davidThread.pendingInput != null) {
                String result = davidThread.pendingInput
                davidThread.pendingInput = null
                davidThread.isWaitingForInput = false
                return result
            } else {
                davidThread.isWaitingForInput = false
                showError(chatId, messageSender, "Sorry, I did not receive the needed input.", "Repeat the command again (${davidThread.commandDescription ?: davidThread.commandName})", davidThread.commandName as String)
                DavidException davidException = new DavidException("Wait for input timeout", false)
                throw davidException
            }
        }
    }

    static void receiveInput(DavidThread davidThread, String value, Long chatId, MessageSender messageSender) {
        if (davidThread == null) {
            showMenu(chatId, messageSender, "Sorry, but I was not expecting this input.")
            DavidException davidException = new DavidException("Unexpected input 1", false)
            throw davidException
        }
        synchronized (davidThread) {
            if (!davidThread.isWaitingForInput) {
                showError(chatId, messageSender, "Sorry, but I was not expecting this input.", "Repeat the previous command (${davidThread.commandDescription ?: davidThread.commandName})", davidThread.commandName as String)
            }
            davidThread.pendingInput = value
            davidThread.isWaitingForInput = false
            davidThread.notifyAll()
        }
    }

    static void showError(Long chatId, MessageSender messageSender, String errorText, String optionName, String commandName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup()
        List<List<InlineKeyboardButton>> inlineKeyboardButtonRows = new ArrayList<>()
        inlineKeyboardButtonRows.add([new InlineKeyboardButton(optionName).setCallbackData("/$commandName")])
        inlineKeyboardButtonRows.add([new InlineKeyboardButton("Menu").setCallbackData("/start")])
        inlineKeyboardMarkup.setKeyboard(inlineKeyboardButtonRows)
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setText(errorText)
                .setReplyMarkup(inlineKeyboardMarkup)
        messageSender.execute(sendMessage)
    }

    static void showMenu(Long chatId, MessageSender messageSender, String errorText) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup()
        List<List<InlineKeyboardButton>> inlineKeyboardButtonRows = new ArrayList<>()
        inlineKeyboardButtonRows.add([new InlineKeyboardButton("Menu").setCallbackData("/start")])
        inlineKeyboardMarkup.setKeyboard(inlineKeyboardButtonRows)
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setText(errorText)
                .setReplyMarkup(inlineKeyboardMarkup)
        messageSender.execute(sendMessage)
    }

}
