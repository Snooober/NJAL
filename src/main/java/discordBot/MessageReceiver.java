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
import tournManager.Tournament;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class MessageReceiver extends ListenerAdapter {
    private boolean confirm = false;
    private ConfirmCommand confirmCommand = null;
    private int roundIdToLoad;
    private String adminMsgToReg = null;

    private boolean isAdmin(MessageReceivedEvent event) {
        List<Role> memberRoles = event.getMember().getRoles();
        return memberRoles.contains(DiscordBot.admin);
    }

    private void clearMsgs(MessageReceivedEvent event) {
        String channelId = event.getChannel().getId();
        if (channelId.equals(DiscordIds.ChannelIds.DRAFT_ME_CHANNEL) || channelId.equals(DiscordIds.ChannelIds.REGISTER_CHANNEL)) {
            if (event.getMessage().getContentRaw().matches("!.*")) {
                event.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
            } else if (event.getAuthor().isBot()) {
                //delete messages except for reg locked/unlocked indicator
                if (!event.getMessage().getContentRaw().matches("Registration is ")) {
                    event.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
                }
            } else if (isAdmin(event)) {
                //do not delete
            } else {
                event.getMessage().delete().queue();
            }
        }
    }

    @Override
    public synchronized void onMessageReceived(MessageReceivedEvent event) {
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
            if (Tournament.getOnGoingTourn() == null) {
                event.getChannel().sendMessage(BotMsgs.noTourn).queue();
            } else {
                int playerId = PlayerLookup.getPlayerId(event.getAuthor().getId());
                Player opponent = PlayerLookup.getCurrentOpponent(playerId);
                event.getChannel().sendMessage(BotMsgs.currentOpponent(opponent)).queue();
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

                //!!reportwin [discord id]
                if (eventMsgStr.matches("!!reportwin \\d*")) {
                    String discordId = eventMsgStr.substring(12);
                    int playerId = PlayerLookup.getPlayerId(discordId);
                    ReportResult.reportResult(event, playerId, true);
                }

                //!!reportloss [discord id]
                if (eventMsgStr.matches("!!reportloss \\d*")) {
                    String discordId = eventMsgStr.substring(13);
                    int playerId = PlayerLookup.getPlayerId(discordId);
                    ReportResult.reportResult(event, playerId, false);
                }

                //!!lockreg
                if (eventMsgStr.equals("!!reg lock")) {
                    RegistrationHandler.lockReg();
                    event.getChannel().sendMessage(BotMsgs.adminLockedReg).queue();
                }

                //!!unlock reg
                if (eventMsgStr.equals("!!reg unlock")) {
                    RegistrationHandler.unlockReg();
                    event.getChannel().sendMessage(BotMsgs.adminUnlockedReg).queue();
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
                if (eventMsgStr.equals("!!send tourn invites")) {
                    startConfirmTimer(ConfirmCommand.SEND_TOURN_INVITES);
                    event.getChannel().sendMessage(BotMsgs.sendTournInvitesConfirm[0]).queue();
                    event.getChannel().sendMessage(BotMsgs.sendTournInvitesConfirm[1]).queue();
                }

                //!!send msg registered [message]
                if (eventMsgStr.matches("!!send\\smsg\\sregistered\\s.*")) {
                    adminMsgToReg = eventMsgStr.split("\\s", 4)[3];
                    startConfirmTimer(ConfirmCommand.SEND_MSG_REGISTERED);
                    event.getChannel().sendMessage(BotMsgs.sendMsgRegisteredConfirm[0]).queue();
                    event.getChannel().sendMessage(BotMsgs.sendMsgRegisteredConfirm[1]).queue();
                }

                //start tournament
                if (eventMsgStr.equals("!!startTourn")) {
                    startConfirmTimer(ConfirmCommand.START_TOURNAMENT);
                    event.getChannel().sendMessage(BotMsgs.startTournConfirm[0]).queue();
                    event.getChannel().sendMessage(BotMsgs.startTournConfirm[1]).queue();
                }

                //!!unregister all
                if (eventMsgStr.equals("!!unregister all")) {
                    startConfirmTimer(ConfirmCommand.UNREGISTER_ALL);
                    event.getChannel().sendMessage(BotMsgs.unregisterAllConfirm[0]).queue();
                    event.getChannel().sendMessage(BotMsgs.unregisterAllConfirm[1]).queue();
                }

                //!!loadTourn #
                if (eventMsgStr.matches("!!loadTourn\\s\\d+") || eventMsgStr.equals("!!loadTourn")) {
                    startConfirmTimer(ConfirmCommand.LOAD_TOURN);

                    String[] response;
                    if (eventMsgStr.matches("!!loadTourn\\s\\d+")) {
                        roundIdToLoad = Integer.parseInt(eventMsgStr.substring(12));
                        response = BotMsgs.loadTournConfirm(roundIdToLoad);
                    } else {
                        //setting "roundIdToLoad = -1" returns the most recent round (highest current_round_id in SQL_CURRENT_T)
                        roundIdToLoad = -1;
                        response = BotMsgs.loadTournConfirm();
                    }
                    event.getChannel().sendMessage(response[0]).queue();
                    event.getChannel().sendMessage(response[1]).queue();
                }

                //!!confirm
                if (eventMsgStr.equals("!!confirm")) {
                    if (confirm) {
                        switch (confirmCommand) {
                            case UNREGISTER_ALL:
                                RegistrationHandler.unregisterAllPlayers();
                                event.getChannel().sendMessage(BotMsgs.unregisteredAllPlayers).queue();
                                break;
                            case START_TOURNAMENT:
                                Tournament.newTournament();
                                event.getChannel().sendMessage(BotMsgs.tournStarted).queue();
                                break;
                            case SEND_TOURN_INVITES:
                                SendMessage.sendTournInvites(event);
                                event.getChannel().sendMessage(BotMsgs.tournInvitesSent).queue();
                                break;
                            case SEND_MSG_REGISTERED:
                                SendMessage.directMsgRegPlayers(adminMsgToReg);
                                event.getChannel().sendMessage(BotMsgs.directMsgToRegSent).queue();
                                break;
                            case LOAD_TOURN:
                                if (Tournament.loadTournament(roundIdToLoad)) {
                                    event.getChannel().sendMessage(BotMsgs.tournLoadedSuccesfully).queue();
                                } else {
                                    event.getChannel().sendMessage(BotMsgs.tournLoadFail).queue();
                                }
                                break;
                        }

                        confirm = false;
                        confirmCommand = null;
                    } else {
                        event.getChannel().sendMessage(BotMsgs.nothingToConfirm).queue();
                    }
                }
            }
        }

        clearMsgs(event);
    }

    private void startConfirmTimer(ConfirmCommand confirmCommand) {
        this.confirm = true;
        this.confirmCommand = confirmCommand;

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                confirm = false;
            }
        };

        timer.schedule(timerTask, 6 * 1000);
    }
}
