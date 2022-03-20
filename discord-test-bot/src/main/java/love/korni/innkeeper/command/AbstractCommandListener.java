package love.korni.innkeeper.command;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

/**
 * AbstractCommand
 *
 * @author Sergei_Konilov
 */
@Slf4j
@Getter
public abstract class AbstractCommandListener implements EventListener {

    public abstract CommandData getCommand();

    protected abstract void onSplashEvent(SlashCommandEvent slashCommandEvent);

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof SlashCommandEvent slashCommandEvent) {
            String name = getCommand().getName();
            if (slashCommandEvent.getName().equals(name)) {
                log.debug("Get splash command: {}, author: {}", name, slashCommandEvent.getUser());
                onSplashEvent(slashCommandEvent);
            }
        }
    }
}
