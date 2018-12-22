package constants;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BotMsgs {
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
