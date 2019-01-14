package tournManager;

import java.util.ArrayList;
import java.util.List;

class Round {
    private List<Game> roundGames;
    private Player byePlayer;
    private int roundId;

    Round(int roundId) {
        this.roundId = roundId;
        roundGames = new ArrayList<>();
        byePlayer = null;
    }

    void addGame(Game game) {
        game.setRound(this);
        roundGames.add(game);
    }

    void setByePlayer(Player byePlayer) {
        this.byePlayer = byePlayer;
    }

    List<Game> getRoundGames() {
        return roundGames;
    }

    Player getByePlayer() {
        return byePlayer;
    }

    public int getRoundId() {
        return roundId;
    }
}
