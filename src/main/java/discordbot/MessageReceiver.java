package discordbot;

import constants.DiscordIds;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageReceiver extends ListenerAdapter {

    private boolean isAdmin(MessageReceivedEvent event) {
        List<Role> memberRoles = event.getMember().getRoles();
        return memberRoles.contains(DiscordBot.admin);
    }

    private void clearMsgs(MessageReceivedEvent event) {
        String channelId = event.getChannel().getId();
        if (channelId.equals(DiscordIds.ChannelIds.DRAFT_ME_CHANNEL) || channelId.equals(DiscordIds.ChannelIds.REGISTER_HERE_CHANNEL)) {
            if (event.getMessage().getContentRaw().matches("!.*")) {
                event.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
            } else if (event.getAuthor().isBot()) {
                event.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
            } else if (isAdmin(event)) {
                //do not delete
            } else {
                event.getMessage().delete().queue();
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        boolean directMessage = event.getChannelType().equals(ChannelType.PRIVATE);
        String eventMsgStr = event.getMessage().getContentRaw();
        String eventChanName = event.getChannel().getName();

        //log message
        if (directMessage) {
            System.out.println("Received direct message from " + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());
        } else {
            System.out.println("Received message from " + event.getAuthor().getName() + " in channel " + eventChanName + ": " + event.getMessage().getContentDisplay());
        }

        //!ping
        if (event.getMessage().getContentRaw().equals("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }

        //!register
        if (eventMsgStr.equals("!register")) {
            RegistrationHandler.registerPlayer(event);
        }

        //!unregister
        if (eventMsgStr.equals("!unregister")) {
            RegistrationHandler.unregisterPlayer(event);
        }


        clearMsgs(event);
    }
}
