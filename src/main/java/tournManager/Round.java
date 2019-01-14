package tournManager;

import java.util.ArrayList;
import java.util.List;

class Round {
    private List<Game> roundGames;
    private Player byePlayer;
    private int roundId;
    private Tournament tournament;

    Round(Tournament tournament) {
        this.tournament = tournament;
        roundGames = new ArrayList<>();
        byePlayer = null;
    }

    void setRoundId(int roundId) {
        this.roundId = roundId;
    }

    void addGame(Player player1, Player player2) {
        Game game = new Game(player1, player2, this, tournament);
        roundGames.add(game);
    }

    void setByePlayer(Player byePlayer) {
        this.byePlayer = byePlayer;
        this.byePlayer.setCurrentGame(null);
    }

    List<Game> getRoundGames() {
        return roundGames;
    }

    Player getByePlayer() {
        return byePlayer;
    }

    int getRoundId() {
        return roundId;
    }
}
