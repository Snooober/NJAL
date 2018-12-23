package discordbot;

import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class LeaveServerListener extends ListenerAdapter {
    @Override
    public void onGuildBan(GuildBanEvent event) {
        RegistrationHandler.unregisterPlayer(event);

    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        RegistrationHandler.unregisterPlayer(event);
    }
}
