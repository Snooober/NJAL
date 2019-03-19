package constants;

import helpers.PlayerLookup;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tournManager.Player;

import static discordBot.DiscordBot.njal;

public class BotMsgs {
    public static final String regLocked = "Registration is currently locked.";
    public static final String dmUnregPlayer = "You have been unregistered from the tournament by an admin.";
    public static final String adminLockedReg = "Registration locked.";
    public static final String adminUnlockedReg = "Registration unlocked.";
    public static final String discordIdNotFound = "Discord ID not found in player_info table.";
    public static final String steamIdAlreadyLinked = "Steam ID already linked to a Discord account.";
    public static final String discordIdhasSteamId = "Discord account is already linked to a Steam ID.";
    public static final String steamIdLinkedAttemptReg = "Steam ID has been linked. Attempting to register player...";
    public static final String steamIdLinkedNotPendReg = "Steam ID has been linked, but player is not pending registration.";
    public static final String steamIdLinkedMultiple = "Steam ID was updated for more than one discord account. See player_info table.";
    public static final String steamIdProblemLinking = "There was a problem linking the steam ID.";
    public static final String tournLinksNotFound = "\"tourn_links.csv\" was not found.";
    public static final String unregisteredAllPlayers = "All players have been unregistered.";
    public static final String steamConnectedAndRegDM = "Your Steam ID has been linked and you have been registered for the tournament!";
    public static final String matchPairMade = "Match completed! Finding another pair of players...";
    public static final String draftMeRoomClosedDM = "Your ***Draft Me!*** room has been closed.";
    public static final String draftMeEntryStarted = "***Draft Me!*** entry started. Please check your direct messages.";
    public static final String notValidMatchCode = "That is not a valid match code.";
    public static final String needToDMRossMatchCode = "You must direct message Ross the bot to accept a ***Draft Me!*** match.";
    public static final String tournStarted = "Tournament has been started.";
    public static final String[] unregisterAllConfirm = new String[2];
    private static final String NJAL_TITLE = ":small_red_triangle_down: **Nicely Jobbed! :thumbsup: Artifact League!** :small_red_triangle_down:";

    static {
        unregisterAllConfirm[0] = "Are you sure you want to unregister all players?";
        unregisterAllConfirm[1] = "Enter `!!confirm` to confirm unregistering all players.";
    }

    public static String currentOpponent(Player opponent) {
        if (opponent == null) {
            return "You have a bye.";
        } else {
            String opponentName = PlayerLookup.getDiscordName(PlayerLookup.getDiscordId(opponent.getPlayerId()));
            return "Your next opponent is: " + opponentName;
        }
    }

    public static String draftMeRoomFoundDM(String channelId) {
        return "***Draft Me!*** room found! See instructions in your ***Draft Me!*** room channel: <#" + channelId + ">";
    }

    public static String draftMeChannelInstructions(String pair1player1, String pair1player2, String pair2player1, String pair2player2) {
        return "Welcome to ***Draft Me!*** where you can play Artifact's draft mode against your friends!\n" +
                "\n" +
                "**" + pair1player1 + "** please make a tournament in the Artifact client. Set the format to `Swiss` and the deck rules to `Registered Call To Arms Draft`. Setting series to `Best of Five` will allow you to play 5 games against your friend before needing to remake the tournament.\n" +
                "When you are done, click `Create Open Invite` (be sure \"Single Use\" is ***not*** checked) and paste it into this chat room so that the rest of the players may join.\n" +
                "\n" +
                "**" + pair1player1 + "** and **" + pair1player2 + "** should click `Search for match` first.\n" +
                "Once they have started a game against each other, " + pair2player1 + " and " + pair2player2 + " may search for a match against each other.\n" +
                "\n" +
                "***Draft Me!*** is designed so that friends may play each other in draft mode. So when you are finished playing the matches against your friend, you may remake another tournament so that friends may continue playing against each other. If one of the 4 players is finished playing, you will need to re-queue in ***Draft Me!***.\n" +
                "However, if all 4 players wish to finish the tournament through rather than only play against each other, feel free to do so!\n" +
                "\n" +
                "*This room will close when any of the 4 players requeue into Draft Me! or go offline.*";

    }

    public static String wrongUserSentMatchCode(int matchCode) {
        return "The match code command needs to be entered by your friend you want to play against (not by you). Tell your friend to message `!match " + matchCode + "` to Ross the bot.";
    }

    public static String queryOpponentMatchCode(int matchCode) {
        return "You have requested a ***Draft Me!*** match. To complete the match, please tell your opponent to direct message Ross the bot: `!match " + matchCode + "`";
    }

    public static String memberJoinNewInfo(String oldName, int oldDiscrim, String newName, int newDiscrim) {
        return oldName + " (#" + oldDiscrim + ") has rejoined the server with a new name. Their new name is " + newName + " (#" + newDiscrim + ").";
    }

    public static String userChangedInfo(String oldName, int oldDiscrim, String newName, int newDiscrim) {
        return oldName + " (#" + oldDiscrim + ") changed their name to " + newName + " (#" + newDiscrim + ").";
    }

    public static String playerLeftGuildAndUnreg(String discordName) {
        return discordName + " has left (or been banned from) NJAL and has been unregistered from the tournament.";
    }

    public static String tournLinkDM(String tournLink) {
        return "Here is your tournament invite link: " + tournLink;
    }

    public static String notEnoughTournLinks(int numLinks, int numPlayers) {
        return "Not enough tournament invite links. Found " + numLinks + " links for " + numPlayers + " players.";
    }

    public static String wasNotReg(String discordId) {
        String name = njal.getMemberById(discordId).getUser().getName();
        return name + " was not registered for the tournament.";

    }

    public static String playerUnreg(String discordName) {
        return discordName + " has been unregistered.";
    }

    public static String playerRegistered(String discordName) {
        return discordName + " has been registered!";
    }

    public static String regQueueSuperAdmin(String discordName) {
        return discordName + " is pending registration.";
    }

    public static String alreadyReg(MessageReceivedEvent event) {
        return event.getAuthor().getName() + " was already registered.";
    }

    public static String regQueueDM(String discordName) {
        return discordName + " has been queued for registration.\n" +
                "\n" +
                "Please make sure you have connected your Steam account to Discord!\n" +
                "(See \"User Settings\" > \"Connections\" in the Discord app.)\n" +
                "You will not be registered until you have connected your Steam account to Discord.\n" +
                "\n" +
                "You will be direct messaged when an admin has completed your registration.\n" +
                "If you have connected your Steam account and your registration has not been completed by the day of the Tournament then please contact an Admin.\n" +
                "If you are registering ***on the day of*** the Tournament then please allow for 1 hour before the tournament (3 PM PST, 23:00 UTC) before contacting an admin.\n" +
                "\n" +
                "You may check your registration status by re-using the !register command.";
    }

    public static String regQueueChan(MessageReceivedEvent event) {
        return event.getAuthor().getName() + " has been queued for registration.\n" +
                "\n" +
                "Please check your DM's from Ross the bot! You will need to link your Steam profile.";
    }

    public static class ReportResult {
        public static final String noTourn = "There is no on-going tournament.";
        public static final String waitingForOpponent = "Waiting for opponent to submit result.";
        public static final String reportConflict = "Player reports differ. Please resend \"!win\" or \"!lose\" to correct result report or contact an admin.";

        private static String resultToString(boolean result) {
            return resultToString(result, false);
        }

        private static String resultToString(boolean result, boolean capitol) {
            if (result) {
                if (capitol) {
                    return "Win";
                } else {
                    return "win";
                }
            } else {
                if (capitol) {
                    return "Loss";
                } else {
                    return "loss";
                }
            }
        }

        public static String youHaveReportedResult(boolean result) {
            return "You have reported a " + resultToString(result) + ".";
        }

        public static String resultAcceptAndProcess(boolean result) {
            return resultToString(result, true) + " accepted and processed.";

        }
    }

    public static class StandingsMsgs {
        public static final String tournStartStandingsMsg = "@Registered ``````\n" +
                "The " + NJAL_TITLE + " Tournament has begun!\n" +
                "\n" +
                "See the player list above.\n" +
                "Ross will direct message you your next opponent.\n" +
                "When you have finished your game, message Ross **!won** or **!lost** to report whether you won or lost the game.\n";
        public static final String finalRoundWinnerDM = "You won today's " + NJAL_TITLE + " Tournament!\n" +
                "Nicely Jobbed :thumbsup:";
        public static final String finalRoundDM = "The " + NJAL_TITLE + " is complete!\n" +
                "\n" +
                "See the standings at <#" + DiscordIds.ChannelIds.STANDINGS_REPORT_CHANNEL + ">";

        public static String tournStartDM(String opponentName) {
            return "The " + NJAL_TITLE + " Tournament has begun!\n" +
                    "Your first opponent is: " + opponentName;
        }

        public static String roundCompleteDM(int currentRoundId, String opponentName) {
            return "Round " + currentRoundId + " has completed!\n" +
                    "Your opponent for " + currentRoundId + 1 + " is: " + opponentName;
        }

        public static String finalRoundStandingsMsg(String winnerName) {
            return "@everyone ``````\n" +
                    "The " + NJAL_TITLE + " Tournament has completed!\n" +
                    "\n" +
                    "Nicely Jobbed :thumbsup: to our winner, " + winnerName + "!\n" +
                    "\n" +
                    "See above for final standings!";
        }

        public static String roundCompleteStandingsMsg(int currentRoundId) {
            return "@Registered ``````\n" +
                    "Round " + currentRoundId + " is complete!\n" +
                    "Ross will direct message you your next opponent.\n" +
                    "\n" +
                    "See above for the current standings!";
        }
    }
}
