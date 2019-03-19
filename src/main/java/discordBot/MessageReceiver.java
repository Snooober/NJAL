package discordBot;

import constants.BotMsgs;
import constants.DiscordIds;
import draftMe.DraftMatcher;
import helpers.PlayerLookup;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import tournManager.Player;
import tournManager.ReportResult;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class MessageReceiver extends ListenerAdapter {
    private boolean confirm = false;

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

        //!draftme
        if (eventMsgStr.equals("!draftme")) {
            DraftMatcher.newDraftMatch(event);
            if (!directMessage) {
                event.getChannel().sendMessage(BotMsgs.draftMeEntryStarted).queue();
            }
        }

        //!match ####
        if (eventMsgStr.matches("!match\\s.*")) {
            if (directMessage) {
                if (eventMsgStr.matches("!match\\s\\d{1,4}")) {
                    Integer matchCode = Integer.parseInt(eventMsgStr.substring(7));
                    DraftMatcher.matchUser(event, matchCode);
                } else {
                    event.getChannel().sendMessage(BotMsgs.notValidMatchCode).queue();
                }
            } else {
                event.getChannel().sendMessage(BotMsgs.needToDMRossMatchCode).queue();
            }
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

        //!won or !win
        if (eventMsgStr.equals("!won") || eventMsgStr.equals("!win")) {
            ReportResult.reportResult(event, true);
        }

        //!lost or !lose
        if (eventMsgStr.equals("!lost") || eventMsgStr.equals("!lose")) {
            ReportResult.reportResult(event, false);
        }

        //!nextgame
        if (eventMsgStr.equals("!nextgame") || eventMsgStr.equals("!next")) {
            int playerId = PlayerLookup.getPlayerId(event.getAuthor().getId());
            Player opponent = PlayerLookup.getCurrentOpponent(playerId);

            event.getChannel().sendMessage(BotMsgs.currentOpponent(opponent)).queue();
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
                    //TODO edit message in register-here channel
                }

                //!!unlock reg
                if (eventMsgStr.equals("!!reg unlock")) {
                    RegistrationHandler.unlockReg();
                    event.getChannel().sendMessage(BotMsgs.adminUnlockedReg).queue();
                    //TODO edit message in register-here channel
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
                    confirm = true;
                    event.getChannel().sendMessage(BotMsgs.unregisterAllConfirm[0]).queue();
                    event.getChannel().sendMessage(BotMsgs.unregisterAllConfirm[1]).queue();

                    class ConfirmFalseTask extends TimerTask {
                        @Override
                        public void run() {
                            confirm = false;
                        }
                    }
                    Timer timer = new Timer();
                    timer.schedule(new ConfirmFalseTask(), 6 * 1000);
                }

                //!!confirm
                if (eventMsgStr.equals("!!confirm")) {
                    if (confirm) {
                        RegistrationHandler.unregisterAllPlayers();
                        event.getChannel().sendMessage(BotMsgs.unregisteredAllPlayers).queue();
                        confirm = false;
                    }
                }
            }
        }

        clearMsgs(event);
    }
}
