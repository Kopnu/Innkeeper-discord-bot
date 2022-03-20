package love.korni.innkeeper.command.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * EventInviteEmbed
 *
 * @author Sergei_Konilov
 */
public class EventInviteEmbed {

    public static final String OFFER_FROM = "Предложение от ";
    public static final String TIME = "Когда:";
    public static final String CONSONANT = "Согласился";
    public static final String DISSENTING = "Отказался";

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private EventInviteEmbed() {
    }

    public static MessageEmbed of(String thumbnailUrl, String title, String desc, String userName, String userAvatarUrl) {
        return of(thumbnailUrl, title, desc, userName, userAvatarUrl, null, List.of(), List.of());
    }

    public static MessageEmbed of(String thumbnailUrl, String title, String desc, String userName, String userAvatarUrl, LocalDate date) {
        return of(thumbnailUrl, title, desc, userName, userAvatarUrl, date, List.of(), List.of());
    }

    public static MessageEmbed of(String thumbnailUrl, String title, String desc, String userName, String userAvatarUrl, List<String> ok, List<String> bad) {
        return of(thumbnailUrl, title, desc, userName, userAvatarUrl, null, ok, bad);
    }

    public static MessageEmbed of(String thumbnailUrl, String title, String desc, String userName, String userAvatarUrl, LocalDate date, List<String> ok, List<String> bad) {
        if (Objects.isNull(ok))
            ok = List.of();
        if (Objects.isNull(bad))
            bad = List.of();
        String consonant = ok.stream().reduce((a, b) -> a + "\n" + b).orElse("");
        String dissenting = bad.stream().reduce((a, b) -> a + "\n" + b).orElse("");
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.decode("0xff9665"))
                .setImage(thumbnailUrl)
                .setAuthor(userName, null, userAvatarUrl)
                .setTitle(title)
                .setDescription(desc)
                .setFooter(OFFER_FROM + userName)
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()));
        if (Objects.nonNull(date)) {
            builder.addField(TIME, date.format(formatter), false);
        }

        builder.addField(CONSONANT, consonant, true)
                .addField(DISSENTING, dissenting, true)
                .addField("", CreateEventCommandListener.OK_SMILE + " - буду\n" + CreateEventCommandListener.SAD_SMILE + " - не буду", false);

        return builder.build();
    }

}
