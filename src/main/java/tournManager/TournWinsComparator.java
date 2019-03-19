package tournManager;

import java.util.Comparator;

public class TournWinsComparator implements Comparator<PlayerRank> {
    @Override
    public int compare(PlayerRank playerRank1, PlayerRank playerRank2) {
        int tournWins = (playerRank2.getTournWins() - playerRank1.getTournWins());
        if (tournWins == 0) {
            return ((playerRank2.getWins() - (playerRank2.getGamesPlayed() - playerRank2.getWins())) - (playerRank1.getWins() - (playerRank1.getGamesPlayed() - playerRank1.getWins())));
        }
        return tournWins;
    }
}
