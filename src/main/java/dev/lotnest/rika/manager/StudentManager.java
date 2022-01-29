package dev.lotnest.rika.manager;

import dev.lotnest.rika.configuration.IdConstants;
import dev.lotnest.rika.configuration.MessageConstants;
import dev.lotnest.rika.utils.MessageUtils;
import dev.lotnest.rika.utils.CommandUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class StudentManager extends ListenerAdapter {

    public static final Pattern STUDENT_PATTERN = Pattern.compile(MessageConstants.STUDENT_REGEX);

    @Override
    public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final MessageChannel channel = guild.getTextChannelById(IdConstants.JOIN_LEAVE_MESSAGES_CHANNEL);
        if (channel == null) {
            return;
        }

        final Member member = event.getMember();
        final EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbedBuilder(member);
        embedBuilder.setTitle(MessageUtils.replacePlaceholders(MessageConstants.NEW_STUDENT_TITLE, guild.getMemberCount()));
        embedBuilder.setDescription(MessageConstants.NEW_STUDENT_DESCRIPTION);
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildMemberRemove(final @NotNull GuildMemberRemoveEvent event) {
        final Guild guild = event.getGuild();
        final MessageChannel channel = guild.getTextChannelById(IdConstants.JOIN_LEAVE_MESSAGES_CHANNEL);
        if (channel == null) {
            return;
        }

        final Member member = event.getMember();
        if (member == null) {
            return;
        }

        final EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbedBuilder(member);
        embedBuilder.setTitle(MessageUtils.replacePlaceholders(MessageConstants.STUDENT_LEFT_TITLE, guild.getMemberCount()));
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildMemberUpdateBoostTime(final @NotNull GuildMemberUpdateBoostTimeEvent event) {
        final Guild guild = event.getGuild();
        final MessageChannel channel = guild.getTextChannelById(IdConstants.BOOST_MESSAGES_CHANNEL);
        if (channel == null) {
            return;
        }

        final Member member = event.getMember();
        final EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbedBuilder(member);
        embedBuilder.setTitle(MessageUtils.replacePlaceholders(MessageConstants.BOOST_MESSAGE_TITLE, guild.getBoostCount()));
        embedBuilder.setDescription(MessageConstants.BOOST_MESSAGE_DESCRIPTION);
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildMessageReceived(final @NotNull GuildMessageReceivedEvent event) {
        CommandUtils.getCommandChannel(event).ifPresent(map -> map.forEach((channel, member) -> {
            if (!channel.getId().equals(IdConstants.VERIFICATION_CHANNEL)) {
                return;
            }

            if (member.getUser().isBot()) {
                return;
            }

            final Guild guild = event.getGuild();
            final Role studentRole = guild.getRoleById(IdConstants.STUDENT_ROLE);
            final EmbedBuilder embedBuilder = MessageUtils.getDefaultEmbedBuilder(member);
            final List<Role> memberRoles = member.getRoles();
            final Role verificatonFailedRole = guild.getRoleById(IdConstants.VERIFICATION_FAILED_ROLE);
            final Message message = event.getMessage();

            if (memberRoles.contains(verificatonFailedRole)) {
                return;
            }

            if (studentRole == null) {
                embedBuilder.setDescription(MessageConstants.ROLE_NOT_FOUND);
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
                message.delete().queue();
                return;
            }

            if (STUDENT_PATTERN.matcher(message.getContentDisplay()).matches()) {
                if (memberRoles.contains(studentRole)) {
                    embedBuilder.setDescription(MessageConstants.ALREADY_VERIFIED);
                    channel.sendMessageEmbeds(embedBuilder.build()).queue();
                    message.delete().queue();
                } else {
                    guild.addRoleToMember(member, studentRole).queue();
                    try {
                        member.getUser().openPrivateChannel().submit()
                                .whenComplete((privateChannel, error) -> {
                                    privateChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                                    if (error != null) {
                                        error.addSuppressed(new IllegalStateException("User has disabled PMs"));
                                    }
                                });

                        embedBuilder.setDescription(MessageConstants.VERIFIED_SUCCESSFULLY);
                        message.delete().queue();
                    } catch (final IllegalStateException e) {
                        message.delete().queue();
                    }
                }
            } else {
                if (!memberRoles.contains(studentRole)) {
                    embedBuilder.setDescription(MessageConstants.FAILED_TO_VERIFY);

                    if (verificatonFailedRole != null) {
                        guild.addRoleToMember(member, verificatonFailedRole).queue();
                    }

                    channel.sendMessageEmbeds(embedBuilder.build()).queue();
                    message.delete().queue();
                }
            }
        }));
    }
}