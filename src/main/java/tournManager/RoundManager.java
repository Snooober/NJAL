package tournManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RoundManager {
    private Tournament tournament;
    private List<Round> roundsList;

    RoundManager(Tournament tournament) {
        this.tournament = tournament;
        roundsList = new ArrayList<>();
    }

    List<Round> getRoundsList() {
        return roundsList;
    }

    void addRound0() {
        List<Player> playerList = tournament.getPlayerList();
        Round round0 = new Round(tournament);

        //Shuffle player list
        Collections.shuffle(playerList);

        //Add 2 players to new game. Add game to round.
        int index = 0;
        while (index < playerList.size() - 1) {
            Player player1 = playerList.get(index);
            Player player2 = playerList.get(index + 1);
            round0.addGame(player1, player2);

            index = index + 2;
        }

        //Manage bye
        if (index == (playerList.size()) - 1) {
            round0.setByePlayer(playerList.get(index));
        }

        addRound(round0);
    }

    private void addRound(Round round) {
        round.setRoundId(roundsList.size());
        roundsList.add(round);
    }
}
