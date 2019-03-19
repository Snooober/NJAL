package tournManager;

import java.util.Comparator;

public class PlayerStandingsComparator implements Comparator<Player> {
    @Override
    public int compare(Player player1, Player player2) {
        int winsMinusGames = ((player1.getNumWins() - player1.getGamesPlayed()) - (player2.getNumWins() - player2.getGamesPlayed()));
        if (winsMinusGames == 0) {
            return (player2.getNumByes() - player1.getNumByes());
        }
        return winsMinusGames;
    }
}
