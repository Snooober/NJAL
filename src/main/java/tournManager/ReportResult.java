package tournManager;

import constants.BotMsgs;
import discordBot.DiscordBot;
import discordBot.SendMessage;
import helpers.PlayerLookup;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ReportResult {

    public static void reportResult(MessageReceivedEvent event, boolean result) {
        String discordId = event.getAuthor().getId();
        int playerId = PlayerLookup.getPlayerId(discordId);
        reportResult(event, playerId, result);
    }

    public static void reportResult(MessageReceivedEvent event, int playerId, boolean result) {
        Tournament tournament = Tournament.getOnGoingTourn();
        if (tournament == null) {
            event.getChannel().sendMessage(BotMsgs.ReportResult.noTourn).queue();
            return;
        }

        Player player = tournament.getPlayerMap().get(playerId);

        //Set result
        WinStatus winStatus = player.setResult(result);
        //Respond to players in Discord
        User opponent = DiscordBot.njal.getMemberById(PlayerLookup.getDiscordId(player.getCurrentOpponent().getPlayerId())).getUser();
        switch (winStatus) {
            case PENDING:
                event.getChannel().sendMessage(BotMsgs.ReportResult.youHaveReportedResult(result)).queue();
                event.getChannel().sendMessage(BotMsgs.ReportResult.waitingForOpponent).queue();
                break;
            case CONFLICT:
                event.getChannel().sendMessage(BotMsgs.ReportResult.reportConflict).queue();
                SendMessage.sendDirectMessage(opponent, BotMsgs.ReportResult.reportConflict);
                break;
            default:
                event.getChannel().sendMessage(BotMsgs.ReportResult.resultAcceptAndProcess(result)).queue();
                SendMessage.sendDirectMessage(opponent, BotMsgs.ReportResult.resultAcceptAndProcess(!result));
                break;
        }

        Tournament.getOnGoingTourn().checkNextRound();
        Tournament.getOnGoingTourn().updateSQL();
    }
}
