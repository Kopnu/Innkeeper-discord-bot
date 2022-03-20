package love.korni.innkeeper.listeners;

import love.korni.jda.spring.stereotype.JdaEventListener;
import love.korni.innkeeper.command.AbstractCommandListener;
import love.korni.innkeeper.domain.Settings;
import love.korni.innkeeper.service.SettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * ConnectToServerEventListener
 *
 * @author Sergei_Konilov
 */
@Slf4j
@JdaEventListener
@RequiredArgsConstructor
public class GuildJoinEventListener implements EventListener {

    private final SettingsService settingsService;
    private final List<AbstractCommandListener> abstractCommands;

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildJoinEvent guildJoinEvent) {
            log.info("onGuildJoinEvent() - start");
            Guild guild = guildJoinEvent.getGuild();
            Settings.GuildSettings guildSettings = settingsService.loadGuildSettings(guild.getId());
            if (Objects.isNull(guildSettings)) {
                settingsService.saveGuildSettings(guild.getId(), new Settings.GuildSettings());
            }

            updateCommands(guild);
            log.info("onGuildJoinEvent() - end");
        }
    }

    private void updateCommands(Guild guild) {
        CommandListUpdateAction commandListUpdateAction = guild.updateCommands();
        abstractCommands.forEach(cmd -> commandListUpdateAction.addCommands(cmd.getCommand()));
        commandListUpdateAction.queue();
    }
}
