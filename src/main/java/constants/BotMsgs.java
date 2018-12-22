package constants;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import static discordbot.DiscordBot.njal;

public class BotMsgs {
    public static String regLocked = "Registration is currently locked.";
    public static String dmUnregPlayer = "You have been unregistered from the tournament by an admin.";

    public static String wasNotReg(String discordId) {
        String name = njal.getMemberById(discordId).getUser().getName();
        return name + " was not registered for the tournament.";

    }

    public static String playerUnreg(String discordName) {
        return discordName + " has been unregistered.";
    }

    public static String playerRegistered(MessageReceivedEvent event) {
        return event.getAuthor().getName() + " has been registered!";
    }

    public static String regQueueSuperAdmin(MessageReceivedEvent event) {
        return event.getAuthor().getName() + " is pending registration.";
    }

    public static String alreadyReg(MessageReceivedEvent event) {
        return event.getAuthor().getName() + " was already registered.";
    }

    public static String regQueueDM(MessageReceivedEvent event) {
        return event.getAuthor().getName() + " has been queued for registration.\n" +
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
