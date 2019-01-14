package discordBot;

import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class UpdateInfoListener extends ListenerAdapter {
    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        RegistrationHandler.updatePlayerInfo(event);
    }

    @Override
    public void onUserUpdateDiscriminator(UserUpdateDiscriminatorEvent event) {
        RegistrationHandler.updatePlayerInfo(event);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        RegistrationHandler.updatePlayerInfo(event);
    }
}