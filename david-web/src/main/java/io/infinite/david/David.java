package io.infinite.david;

import io.infinite.david.telegram.DavidException;
import io.infinite.david.telegram.DavidThread;
import io.infinite.david.telegram.ErrorSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class David extends AbilityBot {

    private static final Logger log = LoggerFactory.getLogger(David.class);

    Integer telegramAdminId;

    Map<Long, DavidThread> threadsByChatId = new HashMap<>();

    @Autowired
    ApplicationContext applicationContext;

    public SilentSender getSilentSender() {
        return silent;
    }

    protected David(
            Integer telegramAdminId,
            String botUsername,
            String botToken

    ) {
        super(botToken, botUsername, ApiContext.getInstance(DefaultBotOptions.class));
        this.telegramAdminId = telegramAdminId;
    }

    @Override
    public int creatorId() {
        return telegramAdminId;
    }

    public Ability receiveInput() {
        return Ability.builder()
                .name(DEFAULT) // DEFAULT ability is executed if user did not specify a command -> Bot needs to have access to messages (check FatherBot)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> receiveInput(ctx.update().getMessage().getText(), ctx.chatId(), silent, sender))
                .build();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if ((!update.hasMessage()) && update.hasCallbackQuery()) {
                if (update.getCallbackQuery().getData().startsWith("/")) {
                    callPlugin(update.getCallbackQuery().getData().split(" ")[0].substring(1),
                            update.getCallbackQuery().getMessage().getChat().getFirstName(),
                            silent,
                            sender,
                            update.getCallbackQuery().getMessage().getChatId(),
                            Arrays.asList(update.getCallbackQuery().getData().split(" "))
                    );
                } else {
                    receiveInput(update.getCallbackQuery().getData(), update.getCallbackQuery().getMessage().getChatId(), silent, sender);
                }
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                if (update.getMessage().getText().startsWith("/")) {
                    callPlugin(update.getMessage().getText().split(" ")[0].substring(1),
                            update.getMessage().getChat().getFirstName(),
                            silent,
                            sender,
                            update.getMessage().getChatId(),
                            Arrays.asList(update.getMessage().getText().split(" "))
                    );
                } else {
                    receiveInput(update.getMessage().getText(), update.getMessage().getChatId(), silent, sender);
                }
            } else if (update.hasMessage() && update.getMessage().hasContact()) {
                receiveInput(update.getMessage().getContact().getPhoneNumber(), update.getMessage().getChatId(), silent, sender);
            } else {
                super.onUpdateReceived(update);
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    void callPlugin(String pluginName, String userName, SilentSender silentSender, MessageSender messageSender, Long chatId, List<String> parameters) {
        if (threadsByChatId.containsKey(chatId)) {
            DavidThread existingDavidThread = threadsByChatId.get(chatId);
            if (existingDavidThread != null) {
                log.debug("Existing thread", existingDavidThread.toString());
                if (existingDavidThread.getWaitingForInput()) {
                    existingDavidThread.interrupt();
                } else {
                    if (existingDavidThread.isAlive()) {
                        ErrorSender.sendError(silentSender, chatId, "Sorry, the previous command (" + existingDavidThread.getCommandName() + ") is still running");
                        return;
                    }
                }
            }
        }
        DavidThread davidThread;
        try {
            davidThread = DavidThread.newDavidThread(
                    userName,
                    silentSender,
                    messageSender,
                    chatId,
                    parameters,
                    applicationContext,
                    pluginName
            );
        } catch (ClassNotFoundException classNotFoundException) {
            silentSender.send("Sorry, this action is not supported.", chatId);
            return;
        }
        applicationContext.getAutowireCapableBeanFactory().autowireBean(davidThread);
        threadsByChatId.put(chatId, davidThread);
        davidThread.start();
    }

    void receiveInput(String updateText, Long chatId, SilentSender silentSender, MessageSender messageSender) {
        try {
            if (threadsByChatId.containsKey(chatId)) {
                threadsByChatId.get(chatId).receiveInput(updateText);
            } else {
                List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();
                List<InlineKeyboardButton> buttons = new ArrayList<>();
                buttons.add(new InlineKeyboardButton("Menu").setCallbackData("/start"));
                buttonRows.add(buttons);
                try {
                    messageSender.execute(new SendMessage()
                            .setChatId(chatId)
                            .setText("Sorry, but I was not expecting this input.")
                            .setReplyMarkup(new InlineKeyboardMarkup(buttonRows))
                    );
                } catch (TelegramApiException telegramApiException) {
                    log.warn("Unexpected TelegramApiException", telegramApiException);
                    silentSender.send("Sorry, but I was not expecting this input.", chatId);
                }
            }
        } catch (DavidException davidException) {
            ErrorSender.sendError(silentSender, chatId, davidException.message);
        }
    }

}
