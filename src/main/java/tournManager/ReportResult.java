package tournManager;

import constants.BotMsgs;
import helpers.PlayerLookup;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ReportResult {

    public static void reportResult(MessageReceivedEvent event, boolean result) {
        String discordId = event.getAuthor().getId();
        int playerId = PlayerLookup.getPlayerId(discordId);

        //TODO
    }

    public static void reportResult(MessageReceivedEvent event, int playerId, boolean result) {
        if (!Tournament.onGoingTourn()) {
            event.getChannel().sendMessage(BotMsgs.ReportResult.noTourn).queue();
            return;
        }

        //TODO here

    }
}
