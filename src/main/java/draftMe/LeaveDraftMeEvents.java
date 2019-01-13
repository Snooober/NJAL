package draftMe;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class LeaveDraftMeEvents extends ListenerAdapter {
    @Override
    public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
        if (event.getNewOnlineStatus().equals(OnlineStatus.INVISIBLE) || event.getNewOnlineStatus().equals(OnlineStatus.OFFLINE) || event.getNewOnlineStatus().equals(OnlineStatus.UNKNOWN)) {
            DraftMatcher.purgeUser(event.getUser());
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        DraftMatcher.purgeUser(event.getUser());
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        DraftMatcher.purgeUser(event.getUser());
    }
}
