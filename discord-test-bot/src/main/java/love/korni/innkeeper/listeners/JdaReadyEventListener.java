package love.korni.innkeeper.listeners;

import love.korni.jda.spring.stereotype.JdaEventListener;
import love.korni.innkeeper.command.AbstractCommandListener;
import love.korni.innkeeper.domain.Settings;
import love.korni.innkeeper.service.SettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JdaReadyEventListener
 *
 * @author Sergei_Konilov
 */
@Slf4j
@JdaEventListener
@RequiredArgsConstructor
public class JdaReadyEventListener implements EventListener {

    private final SettingsService settingsService;
    private final List<AbstractCommandListener> abstractCommands;

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof ReadyEvent readyEvent) {
            log.info("onReadyEvent() - start");
            Settings load = settingsService.load();
            if (Objects.isNull(load.getGuildSettings())) {
                Map<String, Settings.GuildSettings> guildSettings = new HashMap<>();
                readyEvent.getJDA().getGuilds().forEach(guild -> guildSettings.put(guild.getId(), new Settings.GuildSettings()));
                load.setGuildSettings(guildSettings);
                settingsService.save(load);
            }

            readyEvent.getJDA().getGuilds().forEach(this::updateCommands);
            log.info("onReadyEvent() - end");
        }
    }

    private void updateCommands(Guild guild) {
        CommandListUpdateAction commandListUpdateAction = guild.updateCommands();
        abstractCommands.forEach(cmd -> commandListUpdateAction.addCommands(cmd.getCommand()));
        commandListUpdateAction.queue();
    }

}
