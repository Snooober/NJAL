package tournManager;

import java.io.Serializable;

public class Game implements Serializable {
    private Player player1;
    private Player player2;
    private int gameId;
    private Round round;
    private WinStatus winStatus;
    private WinStatus player1Report;
    private WinStatus player2Report;

    Game(Player player1, Player player2, Round round, Tournament tournament) {
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

    boolean isComplete() {
        return (winStatus.equals(WinStatus.PLAYER1) || winStatus.equals(WinStatus.PLAYER2));
    }

    private WinStatus updateWinStatus() {
        if (!player1Report.equals(WinStatus.PENDING) || !player2Report.equals(WinStatus.PENDING)) {
            if (player1Report.equals(player2Report)) {
                if (player1Report.equals(WinStatus.PLAYER1)) {
                    //player1 won
                    this.winStatus = WinStatus.PLAYER1;
                    player1.addWin();
                    player2.addLoss();
                } else if (player1Report.equals(WinStatus.PLAYER2)) {
                    //player 2 won
                    this.winStatus = WinStatus.PLAYER2;
                    player1.addLoss();
                    player2.addWin();
                }
            } else {
                this.winStatus = WinStatus.CONFLICT;
            }
        }

        return winStatus;
    }

    WinStatus setPlayerReport(Player player, boolean result) {
        if (player.equals(player1)) {
            if (result) {
                player1Report = WinStatus.PLAYER1;
            } else {
                player1Report = WinStatus.PLAYER2;
            }
        } else if (player.equals(player2)) {
            if (result) {
                player2Report = WinStatus.PLAYER2;
            } else {
                player2Report = WinStatus.PLAYER1;
            }
        }

        return updateWinStatus();
    }
}
