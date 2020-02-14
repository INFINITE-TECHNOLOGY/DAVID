package io.infinite.david.other

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.david.repositories.LinkRepository
import org.apache.commons.lang3.StringUtils
import org.telegram.abilitybots.api.sender.MessageSender
import org.telegram.abilitybots.api.sender.SilentSender

@BlackBox
@Slf4j
class DavidThread extends Thread {

    String commandName
    String commandDescription
    String userName
    SilentSender silentSender
    MessageSender messageSender
    Long chatId
    List<String> parameters
    GroovyScriptEngine groovyScriptEngine
    LinkRepository linkRepository
    String pendingInput
    Boolean isWaitingForInput

    DavidThread(String commandName, String userName, SilentSender silentSender, MessageSender messageSender, Long chatId, List<String> parameters, GroovyScriptEngine groovyScriptEngine, LinkRepository linkRepository) {
        super(new ThreadGroup("DAVID"), "PLUGIN_" + StringUtils.capitalize(commandName) + "_" + chatId)
        this.commandName = commandName
        this.userName = userName
        this.silentSender = silentSender
        this.messageSender = messageSender
        this.chatId = chatId
        this.parameters = parameters
        this.groovyScriptEngine = groovyScriptEngine
        this.linkRepository = linkRepository
    }

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    void run() {
        Binding binding = new Binding()
        binding.setVariable("silentSender", silentSender)
        binding.setVariable("userName", userName)
        binding.setVariable("messageSender", messageSender)
        binding.setVariable("linkRepository", linkRepository)
        binding.setVariable("parameters", parameters)
        binding.setVariable("chatId", chatId)
        try {
            groovyScriptEngine.run(StringUtils.capitalize(commandName) + "Plugin.groovy", binding)
        } catch (ResourceException | ScriptException e) {
            log.error("David plugin run exception", e)
            AdditionalInputController.showMenu(chatId, messageSender, "Sorry, but I was unable to perform this ability.")
        } catch (DavidException davidException) {
            if (davidException.showToUser) {
                AdditionalInputController.showError(chatId, messageSender, davidException.message, "Try again? (${commandDescription ?: commandName})", commandName as String)
            }
        }
    }

}
