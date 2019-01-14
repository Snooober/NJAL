package tournManager;

class Player {
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
    }

    int getPlayerId() {
        return playerId;
    }

    int getNumWins() {
        return numWins;
    }

    int getGamesPlayed() {
        return gamesPlayed;
    }

    int getNumByes() {
        return numByes;
    }

    Game getCurrentGame() {
        return currentGame;
    }
}
