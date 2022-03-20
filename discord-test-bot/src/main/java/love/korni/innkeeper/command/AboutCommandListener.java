package love.korni.innkeeper.command;

import love.korni.jda.spring.stereotype.JdaEventListener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * AboutChannelCommand
 *
 * @author Sergei_Konilov
 */
@Slf4j
@JdaEventListener
public class AboutCommandListener extends AbstractCommandListener {

    private static final CommandData COMMAND_DATA = new CommandData("about", "Get information about Vi bot");
    private static final String CONTENT = """
            I'm a bot!
            """;

    @Override
    public CommandData getCommand() {
        return COMMAND_DATA;
    }

    @Override
    protected void onSplashEvent(SlashCommandEvent commandEvent) {
        commandEvent.reply(CONTENT).queue();
        log.debug("onSplashEvent() - reply");
    }
}
