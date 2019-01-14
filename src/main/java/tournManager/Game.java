package tournManager;

class Game {
    private Player player1;
    private Player player2;
    private int gameId;
    private Round round;
    private WinStatus winStatus;
    private WinStatus player1Report;
    private WinStatus player2Report;

    Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameId = Tournament.getCurrentGameId();
        Tournament.incrementGameId();
        this.winStatus = WinStatus.PENDING;
        this.player1Report = WinStatus.PENDING;
        this.player2Report = WinStatus.PENDING;
    }

    void setRound(Round round) {
        this.round = round;
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
