package love.korni.innkeeper.command;

import static love.korni.innkeeper.utils.Constants.IDK_COMMAND;

import love.korni.jda.spring.stereotype.JdaEventListener;
import love.korni.innkeeper.domain.Settings;
import love.korni.innkeeper.service.SettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Objects;

/**
 * CreateInfoChannelCommandListener
 *
 * @author Sergei_Konilov
 */
@Slf4j
@JdaEventListener
@RequiredArgsConstructor
public class RegisterChannelCommandListener extends AbstractCommandListener {

    private static final String INFO = "info";
    private static final String LOG = "log";

    private static final CommandData COMMAND_DATA =
            new CommandData("register", "Register current channel");
    private static final SubcommandData INFO_COMMAND_DATA = new SubcommandData(INFO, "Register current channel as info channel");
    private static final SubcommandData LOG_COMMAND_DATA = new SubcommandData(LOG, "Register current channel as log channel");

    private final SettingsService settingsService;

    @Override
    public CommandData getCommand() {
        return COMMAND_DATA.setDefaultEnabled(false).addSubcommands(INFO_COMMAND_DATA, LOG_COMMAND_DATA);
    }

    @Override
    protected void onSplashEvent(SlashCommandEvent slashCommandEvent) {
        String subcommand = slashCommandEvent.getSubcommandName();
        log.info("onSplashEvent() - subcommand: {}", subcommand);
        if (Objects.nonNull(subcommand)) {
            Settings settings = settingsService.load();
            Settings.GuildSettings guildSettings = settings.getGuildSettings().get(slashCommandEvent.getGuild().getId());
            switch (subcommand) {
                case INFO -> {
                    guildSettings.setLogTextChatId(slashCommandEvent.getChannel().getId());
                    slashCommandEvent.reply("This channel registered as Info channel").queue();
                }
                case LOG -> {
                    guildSettings.setInfoTextChatId(slashCommandEvent.getChannel().getId());
                    slashCommandEvent.reply("This channel registered as Log channel").queue();
                }
                default -> slashCommandEvent.reply(IDK_COMMAND).queue();
            }
            settingsService.save(settings);
        }
    }
}
