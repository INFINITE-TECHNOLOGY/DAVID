package io.infinite.david;

import io.infinite.david.other.DavidPluginController;
import io.infinite.david.repositories.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

public class David extends AbilityBot {

    private static final Logger log = LoggerFactory.getLogger(David.class);

    DavidPluginController davidPluginController;

    Integer telegramAdminId;

    LinkRepository linkRepository;

    public SilentSender getSilentSender() {
        return silent;
    }

    protected David(Integer telegramAdminId, String botUsername, String botToken, DefaultBotOptions botOptions, String davidPluginsDir, LinkRepository linkRepository) {
        super(botToken, botUsername, botOptions);
        this.telegramAdminId = telegramAdminId;
        this.davidPluginController = new DavidPluginController(davidPluginsDir, this, linkRepository);
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
                .action(ctx -> davidPluginController.receiveInput(ctx.update().getMessage().getText(), ctx.chatId(), silent, sender))
                .build();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if ((!update.hasMessage()) && update.hasCallbackQuery()) {
                if (update.getCallbackQuery().getData().startsWith("/")) {
                    davidPluginController.callPlugin(update.getCallbackQuery().getData().split(" ")[0].substring(1),
                            update.getCallbackQuery().getMessage().getChat().getFirstName(),
                            silent,
                            sender,
                            update.getCallbackQuery().getMessage().getChatId(),
                            Arrays.asList(update.getCallbackQuery().getData().split(" "))
                    );
                } else {
                    davidPluginController.receiveInput(update.getCallbackQuery().getData(), update.getCallbackQuery().getMessage().getChatId(), silent, sender);
                }
            } else if (update.hasMessage()) {
                if (update.getMessage().getText().startsWith("/")) {
                    davidPluginController.callPlugin(update.getMessage().getText().split(" ")[0].substring(1),
                            update.getMessage().getChat().getFirstName(),
                            silent,
                            sender,
                            update.getMessage().getChatId(),
                            Arrays.asList(update.getMessage().getText().split(" "))
                    );
                } else {
                    davidPluginController.receiveInput(update.getMessage().getText(), update.getMessage().getChatId(), silent, sender);
                }
            } else {
                super.onUpdateReceived(update);
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

}
