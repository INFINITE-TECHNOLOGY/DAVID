package io.infinite.david.telegram


import groovy.transform.CompileDynamic
import org.telegram.abilitybots.api.sender.SilentSender

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class ErrorSender {

    static ConcurrentHashMap<Long, Date> lastErrorDateByChatId = new ConcurrentHashMap<>()

    @CompileDynamic
    static synchronized void sendError(SilentSender silentSender, Long chatId, String text) {
        if (lastErrorDateByChatId.containsKey(chatId)) {
            if (lastErrorDateByChatId.get(chatId).after((Instant.now() - Duration.ofSeconds(3)).toDate())) {
                return
            }
        }
        lastErrorDateByChatId.put(chatId, new Date())
        silentSender.send(text, chatId)
    }

}
