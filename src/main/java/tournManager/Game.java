package tournManager;

class Game {
    private Player player1;
    private Player player2;
    private int gameId;
    private Round round;
    private WinStatus winStatus;
    private WinStatus player1Report;
    private WinStatus player2Report;

    Game(Player player1, Player player2,Round round, Tournament tournament) {
        this.player1 = player1;
        this.player2 = player2;
        this.round = round;
        this.player1.setCurrentGame(this);
        this.player2.setCurrentGame(this);
        this.gameId = tournament.getCurrentGameId();
        tournament.incrementGameId();
        this.winStatus = WinStatus.PENDING;
        this.player1Report = WinStatus.PENDING;
        this.player2Report = WinStatus.PENDING;
    }

    Round getRound() {
        return round;
    }

    int getGameId() {
        return gameId;
    }

    Player getPlayer1() {
        return player1;
    }

    Player getPlayer2() {
        return player2;
    }

    WinStatus getWinStatus() {
        return winStatus;
    }

    WinStatus getPlayer1Report() {
        return player1Report;
    }

    WinStatus getPlayer2Report() {
        return player2Report;
    }
}
