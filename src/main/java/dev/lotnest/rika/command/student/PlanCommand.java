package dev.lotnest.rika.command.student;

import dev.lotnest.rika.command.AbstractCommand;
import dev.lotnest.rika.command.CommandInfo;
import dev.lotnest.rika.command.CommandType;
import dev.lotnest.rika.configuration.CommandConstants;
import dev.lotnest.rika.utils.MessageUtils;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class PlanCommand extends AbstractCommand {

    @Override
    public @NotNull String getName() {
        return CommandConstants.PLAN;
    }

    @Override
    public @NotNull CommandType getCommandType() {
        return CommandType.STUDENT;
    }

    @Override
    public void execute(@NotNull CommandInfo commandInfo) {
        Message message = commandInfo.getMessage();
        MessageUtils.sendNextLessonMessage(commandInfo);
        message.delete().queue();
    }
}
