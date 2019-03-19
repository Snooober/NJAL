package tournManager;

import java.util.ArrayList;
import java.util.List;

public class Round {
    private List<Game> roundGames;
    private Player byePlayer;
    private int roundId;
    private Tournament tournament;

    Round(Tournament tournament) {
        this.tournament = tournament;
        this.roundId = tournament.getRoundsList().size();
        roundGames = new ArrayList<>();
        byePlayer = null;
        tournament.getRoundsList().add(this);
    }

    void addGame(Player player1, Player player2) {
        Game game = new Game(player1, player2, this, tournament);
        roundGames.add(game);
    }

    public List<Game> getRoundGames() {
        return roundGames;
    }

    public Player getByePlayer() {
        return byePlayer;
    }

    void setByePlayer(Player byePlayer) {
        this.byePlayer = byePlayer;
        byePlayer.setCurrentGame(null);
        byePlayer.addBye();
    }

    public int getRoundId() {
        return roundId;
    }
}
