package tournManager;

public class Player {
    private int playerId;
    private int numWins;
    private int gamesPlayed;
    private int numByes;
    private Game currentGame;

    void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    Player(int playerId) {
        this.playerId = playerId;
        this.numWins = 0;
        this.gamesPlayed = 0;
        this.numByes = 0;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getNumWins() {
        return numWins;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getNumByes() {
        return numByes;
    }

    void addWin() {
        numWins++;
        gamesPlayed++;
    }

    void addLoss() {
        gamesPlayed++;
    }

    void addBye() {
        numByes++;
    }

    public Player getCurrentOpponent() {
        if (currentGame==null) {
            return null;
        }

        Player player1 = currentGame.getPlayer1();
        Player player2 = currentGame.getPlayer2();
        if (player1.equals(this)) {
            return player2;
        } else {
            return player1;
        }
    }

    Game getCurrentGame() {
        return currentGame;
    }

    WinStatus setResult(boolean result) {
        return currentGame.setPlayerReport(this, result);
    }
}
