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
        Tournament tournament = Tournament.getOnGoingTourn();
        if (tournament == null) {
            event.getChannel().sendMessage(BotMsgs.ReportResult.noTourn).queue();
            return;
        }

        Player player = tournament.getPlayerMap().get(playerId);

        WinStatus winStatus = player.setResult(result);
        switch (winStatus) {
            case PENDING:
                event.getChannel().sendMessage("You have reported a win.").queue();
                event.getChannel().sendMessage("Waiting for opponent to submit result.").queue();
                break;
            case PLAYER1:
                event.getChannel().sendMessage("Win accepted and processed.").queue();

                //TODO here
        }




    }
}
