package love.korni.innkeeper.command.event;

import love.korni.jda.spring.stereotype.JdaEventListener;
import love.korni.innkeeper.command.AbstractCommandListener;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CreateEventCommandListener
 *
 * @author Sergei_Konilov
 */
@Slf4j
@JdaEventListener
public class CreateEventCommandListener extends AbstractCommandListener {

    static final String OK_SMILE = "\uD83D\uDE42";
    static final String SAD_SMILE = "\uD83D\uDE41";

    private static final List<OptionData> OPTIONS = List.of(
            new OptionData(OptionType.STRING, "name", "Наименование того, что предлагаешь", true),
            new OptionData(OptionType.STRING, "description", "Описание того, что предлагаешь", true),
            new OptionData(OptionType.INTEGER, "days", "Количество дней от сегодняшнего, когда предлагается сыграть. Например: 0 - сегодня, 1 - завтра и тд.", false),
            new OptionData(OptionType.ROLE, "role", "Какую роль тегнуть", false)
    );
    private static final CommandData COMMAND_DATA = new CommandData("create-invite", "Предложить предложение").addOptions(OPTIONS);

    // Pattern for recognizing a URL, based off RFC 3986
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public CommandData getCommand() {
        return COMMAND_DATA;
    }

    @Override
    protected void onSplashEvent(SlashCommandEvent slashCommandEvent) {
        Member member = slashCommandEvent.getMember();
        List<OptionMapping> options = slashCommandEvent.getOptions();

        OptionsContainer container = new OptionsContainer(options);

        MessageEmbed messageEmbed = EventInviteEmbed.of(
                getMetaOgImage(extractUrl(container.description)),
                container.name,
                container.description,
                member.getUser().getName(),
                member.getUser().getAvatarUrl(),
                container.date);


        MessageBuilder messageBuilder = new MessageBuilder();
        if (Objects.nonNull(container.role)) {
            messageBuilder.setContent(container.role.getAsMention());
        }
        Message message = messageBuilder.setEmbeds(messageEmbed).build();
        slashCommandEvent.reply(message).queue(interactionHook -> {
            interactionHook.retrieveOriginal().queue(message1 -> {
                message1.addReaction(OK_SMILE)
                        .and(message1.addReaction(SAD_SMILE))
                        .queue();
            });
        });
    }

    @SneakyThrows
    private String getMetaOgImage(List<String> urls) {
        for (String url : urls) {
            try {
                Document document = Jsoup.connect(url).get();
                String content = document.select("meta[property=og:image]").first().attr("content");
                log.trace("getMetaOgImage() - og:image: {}", content);
                return content;
            } catch (Exception e) {
                log.warn("getMetaOgImage() - error: {}", e.getMessage());
            }
        }
        return null;
    }

    private List<String> extractUrl(String desc) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = URL_PATTERN.matcher(desc);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            urls.add(desc.substring(matchStart, matchEnd));
        }
        log.trace("extractUrl() - urls: {}", urls);
        return urls;
    }

    @Data
    private static class OptionsContainer {
        private String name;
        private String description;
        private LocalDate date;
        private Role role;

        public OptionsContainer(List<OptionMapping> optionMappings) {
            this.name = getByName(optionMappings, "name").get().getAsString();
            this.description = getByName(optionMappings, "description").get().getAsString();
            getByName(optionMappings, "days")
                    .ifPresent((optionMapping) -> {
                        long days = optionMapping.getAsLong();
                        this.date = LocalDate.now(ZoneId.systemDefault()).plusDays(days);
                    });
            getByName(optionMappings, "role")
                    .ifPresent(optionMapping -> this.role = optionMapping.getAsRole());
        }

        private Optional<OptionMapping> getByName(List<OptionMapping> options, String name) {
            return options.stream().filter(option -> option.getName().equals(name)).findFirst();
        }
    }


}
