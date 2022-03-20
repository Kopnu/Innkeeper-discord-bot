package love.korni.innkeeper.command.event;

import static love.korni.innkeeper.command.event.CreateEventCommandListener.OK_SMILE;
import static love.korni.innkeeper.command.event.CreateEventCommandListener.SAD_SMILE;
import static love.korni.innkeeper.command.event.EventInviteEmbed.OFFER_FROM;
import static love.korni.innkeeper.command.event.EventInviteEmbed.TIME;
import static love.korni.innkeeper.command.event.EventInviteEmbed.formatter;

import love.korni.jda.spring.stereotype.JdaEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * CreateEventReactionEventListener
 *
 * @author Sergei_Konilov
 */
@Slf4j
@JdaEventListener
@RequiredArgsConstructor
public class CreateEventReactionEventListener extends ListenerAdapter {

    @Lazy
    private final JDA jda;

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        handleReaction(event, false);
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        handleReaction(event, true);
    }

    private void handleReaction(GenericMessageReactionEvent event, boolean isRemove) {
        log.debug("handleReaction() - event = {}", event);
        if (event.getReaction().isSelf()) {
            return;
        }

        Message message = event.retrieveMessage().complete();
        if (message.getAuthor().equals(event.getUser())) {
            return;
        }

        List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds.size() != 1) {
            return;
        }

        MessageEmbed messageEmbed = embeds.get(0);
        if (messageEmbed.getFooter() == null
                || messageEmbed.getFooter().getText() == null
                || !messageEmbed.getFooter().getText().contains(OFFER_FROM)) {
            return;
        }

        String reactionName = event.getReaction().getReactionEmote().getName();
        switch (reactionName) {
            case OK_SMILE -> {
                if (!isRemove) {
                    message.getReactions().stream()
                            .filter(reaction -> reaction.getReactionEmote().getName().equals(SAD_SMILE))
                            .findFirst().ifPresent(reaction -> reaction.removeReaction(event.getUser()).queue());
                }
                Map<String, List<String>> reactionMap = getReactions(message);
                MessageEmbed updatedMessageEmbed = changeConsonant(messageEmbed, reactionMap.get(OK_SMILE), reactionMap.get(SAD_SMILE));
                message.editMessageEmbeds(updatedMessageEmbed).queue();
            }
            case SAD_SMILE -> {
                if (!isRemove) {
                    message.getReactions().stream()
                            .filter(reaction -> reaction.getReactionEmote().getName().equals(OK_SMILE))
                            .findFirst().ifPresent(reaction -> reaction.removeReaction(event.getUser()).queue());
                }
                Map<String, List<String>> reactionMap = getReactions(message);
                MessageEmbed updatedMessageEmbed = changeConsonant(messageEmbed, reactionMap.get(OK_SMILE), reactionMap.get(SAD_SMILE));
                message.editMessageEmbeds(updatedMessageEmbed).queue();
            }
            default -> event.getReaction().removeReaction().queue();
        }
    }

    private Map<String, List<String>> getReactions(Message message) {
        return message.getReactions().stream()
                .collect(Collectors.toMap(
                        reaction -> reaction.getReactionEmote().getName(),
                        reaction -> reaction.retrieveUsers().complete().stream()
                                .filter(user -> !user.equals(jda.getSelfUser()))
                                .map(User::getName)
                                .toList())
                );
    }

    private MessageEmbed changeConsonant(MessageEmbed messageEmbed, List<String> consonant, List<String> dissenting) {
        Optional<MessageEmbed.Field> first = messageEmbed.getFields().stream().filter(field -> field.getName().equals(TIME)).findFirst();
        LocalDate date = null;
        if (first.isPresent()) {
            String value = first.get().getValue();
            if (Objects.nonNull(value)) {
                date = LocalDate.from(formatter.parse(value));
            }
        }
        return EventInviteEmbed.of(
                getOrNull(() -> messageEmbed.getImage().getUrl()),
                messageEmbed.getTitle(),
                messageEmbed.getDescription(),
                getOrNull(() -> messageEmbed.getAuthor().getName()),
                getOrNull(() -> messageEmbed.getAuthor().getIconUrl()),
                date,
                consonant,
                dissenting);
    }

    private <T> T getOrNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("getOrNull() - error: {}", e.getMessage());
            return null;
        }
    }
}
