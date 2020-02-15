package io.infinite.david

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.david.conf.DavidConfiguration
import io.infinite.david.repositories.LinkRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.FileSystemResource
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
@Slf4j
class DavidApp implements CommandLineRunner {

    @Value('${davidConfFile}')
    FileSystemResource davidConfigResource

    static void main(String[] args) {
        SpringApplication.run(DavidApp.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        runWithLogging()
    }

    static SilentSender silentSender

    @Autowired
    LinkRepository linkRepository

    @BlackBox
    void runWithLogging() {
        log.info("Starting David...")
        if (System.getenv("DavidConfJson") == null) {
            DavidConfiguration.initConfiguration(davidConfigResource)
        } else {
            DavidConfiguration.initConfiguration(System.getenv("DavidConfJson"))
        }
        ApiContextInitializer.init()
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi()
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class)
        if (DavidConfiguration.conf.telegramProxyHost != null) {
            log.info("Using proxy: " +
                    "$DavidConfiguration.conf.telegramProxyHost" +
                    ":$DavidConfiguration.conf.telegramProxyPort")
            botOptions.setProxyHost(DavidConfiguration.conf.telegramProxyHost)
            botOptions.setProxyPort(DavidConfiguration.conf.telegramProxyPort)
            botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP)
        }
        David david = new David(new Integer(System.getenv("telegramAdminId")), DavidConfiguration.conf.botUsername, System.getenv("botToken"), botOptions, DavidConfiguration.conf.pluginsDir, linkRepository)
        telegramBotsApi.registerBot(david)
        silentSender = david.getSilentSender()
    }

}
