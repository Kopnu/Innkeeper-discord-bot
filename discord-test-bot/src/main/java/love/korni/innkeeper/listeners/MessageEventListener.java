package love.korni.innkeeper.listeners;

import love.korni.jda.spring.stereotype.JdaEventListener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * MessageEventListener
 *
 * @author Sergei_Konilov
 */
@Slf4j
@JdaEventListener
public class MessageEventListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        log.info("Receive message: {}", message);
    }
}
