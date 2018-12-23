package constants;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import static discordbot.DiscordBot.njal;

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
    public static final String[] unregisterAllConfirm = new String[2];

    static {
        unregisterAllConfirm[0] = "Are you sure you want to unregister all players?";
        unregisterAllConfirm[1] = "Enter `!!confirm` to confirm unregistering all players.";
    }

    public static String memberJoinNewInfo(String oldName, int oldDiscrim, String newName, int newDiscrim) {
        return oldName + " (#" + oldDiscrim + ") has rejoined the server with a new name. Their new name is " + newName + " (#" + newDiscrim + ").";
    }

    public static String userUpdatedName(String oldName, String newName) {
        return oldName + " changed their name to " + newName + ".";
    }

    public static String userUpdatedDiscrim(String oldDiscrim, int newDiscrim) {
        return oldDiscrim + " changed their discriminator to " + newDiscrim + ".";
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
}
