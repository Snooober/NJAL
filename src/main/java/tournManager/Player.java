package tournManager;

class Player {
    private int playerId;
    private int numWins;
    private int gamesPlayed;
    private int numByes;

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
}
