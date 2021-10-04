package lotnest.rika.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lotnest.rika.Rika.MAIN_COLOR;
import static lotnest.rika.Rika.PREFIX;
import static lotnest.rika.configuration.Message.FOOTER;
import static lotnest.rika.util.MemberUtil.getNameAndTag;

public class MessageUtil {

    public static boolean isValidCommand(@Nullable final String message) {
        return isValidCommand(message, null);
    }

    public static boolean isValidCommand(@Nullable final String message, final @Nullable String expectedCommand) {
        return message != null && message.startsWith(PREFIX + (expectedCommand != null ? expectedCommand : ""));
    }

    public static String @NotNull [] getArguments(@Nullable final String command) {
        if (!isValidCommand(command)) {
            return new String[]{};
        }
        return command.replaceFirst(PREFIX, "").split(" ");
    }

    public static String replacePlaceholders(@NotNull String message, final Object @Nullable ... objects) {
        if (objects == null) {
            return message;
        }
        
        int i = 0;
        for (final Object object : objects) {
            message = message.replace("{" + i + "}", objects[i++].toString());
        }
        return message;
    }

    public static @NotNull EmbedBuilder getDefaultEmbedBuilder(@NotNull final Member member) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MAIN_COLOR);
        embedBuilder.setAuthor(getNameAndTag(member), null, member.getUser().getAvatarUrl());
        embedBuilder.setFooter(FOOTER);
        return embedBuilder;
    }
}
