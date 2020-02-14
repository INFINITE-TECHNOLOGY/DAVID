package io.infinite.david.other


import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.david.repositories.LinkRepository
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.bots.AbsSender

@BlackBox
@Slf4j
class DavidPluginController {

    GroovyScriptEngine groovyScriptEngine

    AbsSender absSender

    Map<Long, DavidThread> threadsByChatId = new HashMap<>()

    LinkRepository linkRepository

    DavidPluginController(String davidPluginsDir, AbsSender absSender, LinkRepository linkRepository) {
        this.groovyScriptEngine = new GroovyScriptEngine(davidPluginsDir, this.getClass().getClassLoader())
        this.absSender = absSender
        this.linkRepository = linkRepository
    }

    void callPlugin(String pluginName, String userName, SilentSender silentSender, MessageSender messageSender, Long chatId, List<String> parameters) {
        if (threadsByChatId.containsKey(chatId)) {
            threadsByChatId.get(chatId).interrupt()
        }
        DavidThread davidThread = new DavidThread(
                pluginName,
                userName,
                silentSender,
                messageSender,
                chatId,
                parameters,
                groovyScriptEngine,
                linkRepository
        )
        threadsByChatId.put(chatId, davidThread)
        davidThread.start()
    }

    void receiveInput(String updateText, Long chatId, SilentSender silentSender, MessageSender messageSender) {
        try {
            AdditionalInputController.receiveInput(threadsByChatId.get(chatId), updateText, chatId, messageSender)
        } catch (DavidException davidException) {
            if (davidException.showToUser) {
                ErrorSender.sendError(silentSender, chatId, davidException.message)
            }
        }
    }


}
