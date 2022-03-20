package love.korni.innkeeper.service.impl;

import love.korni.innkeeper.domain.Settings;
import love.korni.innkeeper.service.SettingsService;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * SettingServiceImpl
 *
 * @author Sergei_Konilov
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private static final String OS = (System.getProperty("os.name")).toUpperCase();
    private static final String SETTINGS_FILE_NAME = "settings.json";

    private static String MY_DIRECTORY;

    private final Gson gson;

    static {
        if (OS.contains("WIN")) {
            MY_DIRECTORY = System.getenv("AppData");
        } else {
            MY_DIRECTORY = System.getProperty("user.home");
        }
        MY_DIRECTORY += "/Korni Love/Discord Base/";
    }

    @SneakyThrows
    @Override
    public void save(Settings settings) {
        Path settingsPath = getLocalFilePath(SETTINGS_FILE_NAME);
        if (Objects.nonNull(settingsPath)) {
            String json = gson.toJson(settings);
            Files.writeString(settingsPath, json);
            log.info("Settings saved: {}", json);
        }
    }

    @SneakyThrows
    @Override
    public Settings load() {
        Settings settings = new Settings();
        Path settingsPath = getLocalFilePath(SETTINGS_FILE_NAME);
        if (Objects.nonNull(settingsPath)) {
            String string = Files.readString(settingsPath);
            if (!string.isBlank()) {
                settings = gson.fromJson(string, Settings.class);
            }
        }
        if (Objects.isNull(settings)) {
            return new Settings();
        }
        return settings;
    }

    @SneakyThrows
    @Override
    public void saveGuildSettings(String guildId, Settings.GuildSettings guildSettings) {
        Settings settings = load();
        settings.getGuildSettings().put(guildId, guildSettings);
        save(settings);
    }

    @SneakyThrows
    @Override
    public Settings.GuildSettings loadGuildSettings(String guildId) {
        Settings load = load();
        Settings.GuildSettings guildSettings = load.getGuildSettings().getOrDefault(guildId, new Settings.GuildSettings());
        log.debug("loadGuildSettings() - return: {}", guildSettings);
        return guildSettings;
    }


    private Path getLocalFolderPath() {
        try {
            return Files.createDirectories(Path.of(MY_DIRECTORY));
        } catch (IOException e) {
            log.error("getLocalFolderPath() - error", e);
        }
        return null;
    }

    private Path getLocalFilePath(String fileName) {
        try {
            Path localFolderPath = getLocalFolderPath();
            Path filePath = Path.of(localFolderPath + "/" + fileName);
            if (!Files.exists(filePath)) {
                return Files.createFile(filePath);
            }
            return filePath;
        } catch (IOException e) {
            log.error("getLocalFilePath() - error", e);
        }
        return null;
    }


    @PreDestroy
    public void preDestroy() {
        Settings settings = load();
        save(settings);
        log.info("preDestroy() - save settings");
    }

}
