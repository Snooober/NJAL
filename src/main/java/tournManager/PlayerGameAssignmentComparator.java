package tournManager;

import org.apache.logging.log4j.util.PropertySource;

import java.util.Comparator;

public class PlayerGameAssignmentComparator implements Comparator<Player> {
    @Override
    public int compare(Player player1, Player player2) {
        int winsMinusGames = ((player1.getNumWins() - player1.getGamesPlayed()) - (player2.getNumWins() - player2.getGamesPlayed()));
        if (winsMinusGames == 0) {
            return (player1.getNumByes() - player2.getNumByes());
        }
        return winsMinusGames;
    }
}
