package tournManager;

public class PlayerRank {
    private int playerId;
    private String discordName;
    private int wins;
    private int gamesPlayed;
    private int byes;
    private int tournWins;

    public PlayerRank(int playerId, String discordName, int wins, int gamesPlayed, int byes, int tournWins) {
        this.playerId = playerId;
        this.discordName = discordName;
        this.wins = wins;
        this.gamesPlayed = gamesPlayed;
        this.byes = byes;
        this.tournWins = tournWins;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getDiscordName() {
        return discordName;
    }

    public int getWins() {
        return wins;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getByes() {
        return byes;
    }

    public int getTournWins() {
        return tournWins;
    }
}
