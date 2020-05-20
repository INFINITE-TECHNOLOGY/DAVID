package io.infinite.david.telegram

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.apache.commons.lang3.StringUtils
import org.springframework.context.ApplicationContext
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@ToString(includeNames = true, includeFields = true)
abstract class DavidThread extends Thread {

    String commandName = this.class.simpleName
    String userName
    SilentSender silentSender
    MessageSender messageSender
    Long chatId
    List<String> parameters
    String pendingInput
    Boolean waitingForInput = false
    ApplicationContext applicationContext
    final Object lock = new Object()

    DavidThread(
            String userName,
            SilentSender silentSender,
            MessageSender messageSender,
            Long chatId,
            List<String> parameters,
            ApplicationContext applicationContext
    ) {
        super(new ThreadGroup(chatId.toString()), "DAVID_PLUGIN")
        this.name = this.class.simpleName
        this.userName = userName
        this.silentSender = silentSender
        this.messageSender = messageSender
        this.chatId = chatId
        this.parameters = parameters
        this.applicationContext = applicationContext
    }

    static DavidThread newDavidThread(
            String userName,
            SilentSender silentSender,
            MessageSender messageSender,
            Long chatId,
            List<String> parameters,
            ApplicationContext applicationContext,
            String pluginName
    ) throws ClassNotFoundException {
        return Class.forName("io.infinite.david.abilities.${pluginName.capitalize()}").newInstance(
                userName,
                silentSender,
                messageSender,
                chatId,
                parameters,
                applicationContext
        ) as DavidThread
    }

    abstract void applyPlugin()

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    final void run() {
        try {
            applyPlugin()
        } catch (AscendUnauthorizedException ignored) {
            showError("Sorry, the authorization was not successful.", "Try again?")
        } catch (DavidUserException davidUserException) {
            showError(davidUserException.message, "Try again?")
        } catch (DavidInterruptedException davidInterruptedException) {
            log.warn("Command interrupted", davidInterruptedException)
        } catch (Exception e) {
            log.error("David plugin run exception", e)
            showMenu("Sorry, but I was unable to perform this ability.")
        }
    }

    String waitForInput(Integer timeoutSeconds) {
        synchronized (lock) {
            waitingForInput = true
            try {
                lock.wait(timeoutSeconds * 1000)
            } catch (InterruptedException interruptedException) {
                throw new DavidInterruptedException("Command interrupted.", interruptedException)
            }
            if (pendingInput != null) {
                String result = pendingInput
                pendingInput = null
                waitingForInput = false
                return result
            } else {
                waitingForInput = false
                throw new DavidUserException("Sorry, I did not receive the needed input.")
            }
        }
    }

    void receiveInput(String value) throws DavidException {
        synchronized (lock) {
            if (!waitingForInput) {
                showError("Sorry, but I was not expecting this input.", "Repeat the previous command?")
            }
            pendingInput = value
            waitingForInput = false
            lock.notifyAll()
        }
    }

    void oneTimeKeyboard(String message, List<KeyboardRow> buttonRows) {
        messageSender.execute(new SendMessage()
                .setChatId(chatId)
                .setText(message)
                .setReplyMarkup(
                        new ReplyKeyboardMarkup(
                                keyboard: buttonRows
                        ).setOneTimeKeyboard(true)
                ))
    }

    void showError(String errorText, String optionName) {
        messageSender.execute(new SendMessage()
                .setChatId(chatId)
                .setText(errorText)
                .setReplyMarkup(new ReplyKeyboardRemove())
        )
        showButtons(optionName, [
                [new InlineKeyboardButton(commandName.capitalize()).setCallbackData("/$commandName")],
                [new InlineKeyboardButton("Menu").setCallbackData("/start")]
        ])
    }

    void showMenu(String text) {
        showButtons(text, [[new InlineKeyboardButton("Menu").setCallbackData("/start")]])
    }

    void showButtons(String text, List<List<InlineKeyboardButton>> buttons) {
        messageSender.execute(new SendMessage()
                .setChatId(chatId)
                .setText(text)
                .setReplyMarkup(
                        new InlineKeyboardMarkup(buttons)
                ))
    }

    Optional<Message> send(String messageTemplate) {
        String finalMessage = messageTemplate.replace("%username%", userName)
        return silentSender.send(finalMessage, chatId)
    }

    void sendMd(String messageTemplate) {
        String finalMessage = messageTemplate.replace("%username%", userName)
        silentSender.sendMd(finalMessage, chatId)
    }

    void forceReply(String messageTemplate) {
        String finalMessage = messageTemplate.replace("%username%", userName)
        silentSender.forceReply(finalMessage, chatId)
    }

    void showButton(String message, String caption, String data) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup()
        List<List<InlineKeyboardButton>> inlineKeyboardButtonRows = new ArrayList<>()
        inlineKeyboardButtonRows.add([new InlineKeyboardButton(caption).setCallbackData(data)])
        inlineKeyboardMarkup.setKeyboard(inlineKeyboardButtonRows)
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setText(message)
                .setReplyMarkup(inlineKeyboardMarkup)
        messageSender.execute(sendMessage)
    }

    void validateDoubleNumber(String number, Integer minValue, Integer maxValue) {
        try {
            Double check = Double.parseDouble(number)
            if (check > maxValue) {
                throw new DavidUserException("Sorry, the value should not exceed ${maxValue}.")
            }
            if (check <= minValue) {
                throw new DavidUserException("Sorry, the value should be greater than ${minValue}")
            }
        } catch (DavidException davidException) {
            throw davidException
        } catch (Exception e) {
            log.warn("Validate Limit Exception", e)
            throw new DavidUserException("Sorry, the value should be numeric.")
        }
    }

    Boolean proceed(String userMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkupConfirmation = new InlineKeyboardMarkup()
        List<List<InlineKeyboardButton>> inlineKeyboardButtonConfirmationRows = new ArrayList<>()
        inlineKeyboardButtonConfirmationRows.add([new InlineKeyboardButton("Proceed").setCallbackData("Proceed")])
        inlineKeyboardButtonConfirmationRows.add([new InlineKeyboardButton("Skip").setCallbackData("Skip")])
        inlineKeyboardMarkupConfirmation.setKeyboard(inlineKeyboardButtonConfirmationRows)
        SendMessage sendMessageConfirmation = new SendMessage()
                .setChatId(chatId)
                .setText(userMessage)
                .setReplyMarkup(inlineKeyboardMarkupConfirmation)
        messageSender.execute(sendMessageConfirmation)
        String confirmation = waitForInput(30)
        if (!["Proceed", "Skip"].contains(confirmation)) {
            return false
        }
        if (confirmation != "Proceed") {
            return false
        }
        return true
    }

    void confirm(String userMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkupConfirmation = new InlineKeyboardMarkup()
        List<List<InlineKeyboardButton>> inlineKeyboardButtonConfirmationRows = new ArrayList<>()
        inlineKeyboardButtonConfirmationRows.add([new InlineKeyboardButton("Confirm").setCallbackData("Confirm")])
        inlineKeyboardButtonConfirmationRows.add([new InlineKeyboardButton("Cancel").setCallbackData("Cancel")])
        inlineKeyboardMarkupConfirmation.setKeyboard(inlineKeyboardButtonConfirmationRows)
        SendMessage sendMessageConfirmation = new SendMessage()
                .setChatId(chatId)
                .setText(userMessage)
                .setReplyMarkup(inlineKeyboardMarkupConfirmation)
        messageSender.execute(sendMessageConfirmation)
        String confirmation = waitForInput(30)
        if (!["Confirm", "Cancel"].contains(confirmation)) {
            throw new DavidUserException("Action cancelled as per you request.")
        }
        if (confirmation != "Confirm") {
            throw new DavidUserException("Action cancelled as per you request.")
        }
    }

    void secretMessage(String unmaskedMessage, String maskedMessage, Integer timeoutMinutes) {
        Optional<Message> optionalMessage = send(unmaskedMessage)
        send("Please note that sensitive details will be automatically masked in the above message after $timeoutMinutes minutes.")
        Message message = optionalMessage.get()
        EditMessageText editMessageText = new EditMessageText()
        editMessageText.setChatId(chatId)
        editMessageText.setMessageId(message.messageId)
        editMessageText.setText(maskedMessage)
        new Thread({
            sleep(timeoutMinutes * 60 * 1000)
            messageSender.execute(editMessageText)
        }).start()
    }

    void validateEmail(String email) {
        if (email.split("@").size() != 2) {
            throw new DavidException("Sorry, invalid email format.")
        }
    }

    void sendKeyboard(List<List<InlineKeyboardButton>> inlineKeyboardButtonRows, String message) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setText(message)
                .setReplyMarkup(new InlineKeyboardMarkup(inlineKeyboardButtonRows))
        messageSender.execute(sendMessage)
    }
}