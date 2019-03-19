package discordBot;

import draftMe.DraftMatcher;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

//TODO fix this
public class LeaveServerListener extends ListenerAdapter {
    @Override
    public void onGuildBan(GuildBanEvent event) {
        RegistrationHandler.unregisterPlayer(event);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        RegistrationHandler.unregisterPlayer(event);
    }

    @Override
    public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
        if (event.getNewOnlineStatus().equals(OnlineStatus.INVISIBLE) || event.getNewOnlineStatus().equals(OnlineStatus.OFFLINE) || event.getNewOnlineStatus().equals(OnlineStatus.UNKNOWN)) {
            DraftMatcher.purgeUser(event.getUser());
        }
    }
}
