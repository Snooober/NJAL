package discordbot;

import constants.BotMsgs;
import constants.DiscordIds;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static discordbot.DiscordBot.njal;

public class MessageReceiver extends ListenerAdapter {
    private boolean unregisterAllConfirm = false;

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

        //lol
        if ((eventMsgStr.toLowerCase().matches(".*bot.*") && eventMsgStr.toLowerCase().matches(".*hearthstone.*")) || (eventMsgStr.toLowerCase().matches(".*ross.*") && eventMsgStr.toLowerCase().matches(".*hearthstone.*"))) {
            String message;
            int rand = new Random().nextInt(100);
            if (rand < 33) {
                message = "I play what I want.";
            } else if (rand < 66) {
                message = "Artifact has too much RNG.";
            } else {
                message = "Artifact is too expensive.";
            }
            event.getChannel().sendMessage(message).queue();
        }

        /*
         * ~~~~~~~~~~~~~~~~~~~~~~~~~ADMIN COMMANDS~~~~~~~~~~~~~~~~~~~~~~~~~
         */

        if (eventChanName.equals("admin-commands") || eventChanName.equals("super-admin")) {
            if (isAdmin(event)) {
                //!!unregister [discord id]
                if (eventMsgStr.matches("!!unregister\\s\\d*")) {
                    String discordId = eventMsgStr.split("\\s")[1];
                    RegistrationHandler.unregisterPlayer(event, discordId);
                }

                //!!lockreg
                if (eventMsgStr.equals("!!reg lock")) {
                    RegistrationHandler.lockReg();
                    event.getChannel().sendMessage(BotMsgs.adminLockedReg).queue();
                    njal.getTextChannelById(DiscordIds.ChannelIds.STANDINGS_REPORT_CHANNEL).sendMessage(BotMsgs.adminLockedReg).queue();
                }

                //!!unlock reg
                if (eventMsgStr.equals("!!reg unlock")) {
                    RegistrationHandler.unlockReg();
                    event.getChannel().sendMessage(BotMsgs.adminUnlockedReg).queue();
                    njal.getTextChannelById(DiscordIds.ChannelIds.STANDINGS_REPORT_CHANNEL).sendMessage(BotMsgs.adminUnlockedReg).queue();
                }

                //!!steam connect [discord id][steam id]
                if (eventMsgStr.matches("!!steam\\sconnect\\s\\d+\\s[\\w:]+")) {
                    String[] messageArray = eventMsgStr.split("\\s");
                    String discordId = messageArray[2];
                    String steamId = messageArray[3];
                    SteamConnector.connectSteamId(event, discordId, steamId);
                }

                //!!steam pending
                if (eventMsgStr.equals("!!steam pending")) {
                    SteamConnector.sendPendRegPlayers(event);
                }

                //!!send tourn invites
                //TODO add confirmation
                if (eventMsgStr.equals("!!send tourn invites")) {
                    SendMessage.sendTournInvites(event);
                }

                //!!send msg registered [message]
                //TODO add confirmation
                if (eventMsgStr.matches("!!send\\smsg\\sregistered\\s.*")) {
                    String message = eventMsgStr.split("\\s", 4)[3];
                    SendMessage.directMsgRegPlayers(message);
                }

                //!!unregister all
                if (eventMsgStr.equals("!!unregister all")) {
                    unregisterAllConfirm = true;
                    event.getChannel().sendMessage(BotMsgs.unregisterAllConfirm[0]).queue();
                    event.getChannel().sendMessage(BotMsgs.unregisterAllConfirm[1]).queue();

                    class ConfirmFalseTask extends TimerTask {
                        @Override
                        public void run() {
                            unregisterAllConfirm = false;
                        }
                    }
                    Timer timer = new Timer();
                    timer.schedule(new ConfirmFalseTask(), 6 * 1000);
                }

                //!!confirm
                if (eventMsgStr.equals("!!confirm")) {
                    if (unregisterAllConfirm) {
                        RegistrationHandler.unregisterAllPlayers();
                        event.getChannel().sendMessage(BotMsgs.unregisteredAllPlayers).queue();
                        unregisterAllConfirm = false;
                    }
                }
            }
        }


        clearMsgs(event);
    }
}
