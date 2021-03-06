package tournManager;

import discordBot.RegistrationHandler;
import discordBot.SendMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RoundManager implements Serializable {
    private Tournament tournament;
    private List<Round> roundsList;

    RoundManager(Tournament tournament) {
        this.tournament = tournament;
        roundsList = new ArrayList<>();
    }

    List<Round> getRoundsList() {
        return roundsList;
    }

    void addRound() {
        List<Player> playerList = tournament.getPlayerList();
        Round newRound = new Round(tournament);

        if (newRound.getRoundId() == 0) {
            //Shuffle player list for round 0.
            Collections.shuffle(playerList);
        } else {
            playerList.sort(new PlayerGameAssignmentComparator());
        }

        //Add 2 players to new game.
        int index = 0;
        while (index < playerList.size() - 1) {
            Player player1 = playerList.get(index);
            Player player2 = playerList.get(index + 1);
            newRound.addGame(player1, player2);

            index = index + 2;
        }

        //Manage bye player
        if (index == (playerList.size()) - 1) {
            newRound.setByePlayer(playerList.get(index));
        }

        roundsList.add(newRound);

        //TODO may need to adjust byes when sending standings
        SendMessage.sendStandings(tournament);

        tournament.saveTourn();
    }

    private void endTourn() {
        tournament.updateOverallStatsSQL();
        SendMessage.updateOverallStatsMsgs();
        SendMessage.sendStandings(tournament);
        tournament.archiveTourn();
        RegistrationHandler.unregisterAllPlayers();
        Tournament.endTourn();
    }

    void checkNextRound() {
        //Return if round is not complete
        Round currentRound = roundsList.get(roundsList.size() - 1);
        for (Game game :
                currentRound.getRoundGames()) {
            if (!game.isComplete()) {
                return;
            }
        }

        //Check for final round
        if (tournament.onFinalRound()) {
            endTourn();
        } else {
            //Transition to a new round
            addRound();
        }
    }
}
