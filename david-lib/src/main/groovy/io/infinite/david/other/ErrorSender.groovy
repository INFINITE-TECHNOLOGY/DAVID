package io.infinite.david.other

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import org.telegram.abilitybots.api.sender.SilentSender

import java.util.concurrent.ConcurrentHashMap

class ErrorSender {

    static ConcurrentHashMap<Long, Date> lastErrorDateByChatId = new ConcurrentHashMap<>()

    @CompileDynamic
    static synchronized void sendError(SilentSender silentSender, Long chatId, String text) {
        if (lastErrorDateByChatId.containsKey(chatId)) {
            Date minSendDate
            use(TimeCategory) {
                minSendDate = new Date() - 3.seconds
            }
            if (lastErrorDateByChatId.get(chatId).after(minSendDate)) {
                return
            }
        }
        lastErrorDateByChatId.put(chatId, new Date())
        silentSender.send(text, chatId)
    }

}
