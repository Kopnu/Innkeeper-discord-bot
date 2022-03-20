package love.korni.innkeeper.service;

import love.korni.innkeeper.domain.Settings;

/**
 * SettingService
 *
 * @author Sergei_Konilov
 */
public interface SettingsService {

    void save(Settings settings);

    Settings load();

    void saveGuildSettings(String guildId, Settings.GuildSettings guildSettings);

    Settings.GuildSettings loadGuildSettings(String guildId);
}
