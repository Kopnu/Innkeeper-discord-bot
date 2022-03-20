package love.korni.innkeeper.domain;

import lombok.Data;

import java.util.Map;

/**
 * Settings
 *
 * @author Sergei_Konilov
 */
@Data
public class Settings {

    private Map<String, GuildSettings> guildSettings;

    @Data
    public static class GuildSettings {
        private String logTextChatId;
        private String infoTextChatId;
    }

}
