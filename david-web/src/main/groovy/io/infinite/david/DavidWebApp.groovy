package io.infinite.david

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi

import java.util.logging.Level
import java.util.logging.Logger

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication(scanBasePackages = ["io.infinite.ascend", "io.infinite.david"])
@EnableJpaRepositories("io.infinite.ascend")
@EntityScan("io.infinite.ascend")
@Slf4j
class DavidWebApp implements CommandLineRunner {

    @Autowired
    ApplicationContext applicationContext

    @Value('${telegramAdminId}')
    Integer telegramAdminId

    @Value('${botUsername}')
    String botUsername

    @Value('${botToken}')
    String botToken

    static void main(String[] args) {
        System.setProperty("jwtAccessKeyPublic", "")
        System.setProperty("jwtAccessKeyPrivate", "")
        System.setProperty("jwtRefreshKeyPublic", "")
        System.setProperty("jwtRefreshKeyPrivate", "")
        System.setProperty("ascendValidationUrl", "")
        SpringApplication.run(DavidWebApp.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        runWithLogging()
    }

    @BlackBox
    void runWithLogging() {
        log.info('Started David.')
        ApiContextInitializer.init()
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi()
        David david = new David(telegramAdminId, botUsername, botToken)
        applicationContext.autowireCapableBeanFactory.autowireBean(david)
        telegramBotsApi.registerBot(david)
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
        Logger.getLogger("").setLevel(Level.FINEST)
    }

}
