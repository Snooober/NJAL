package tournManager;

import java.util.List;

//TODO should keep a playerList member variable and keep track of current game/round/etc. then update SQL tourn_players from this list instead of current round.
public class Tournament {
    private static Tournament tournament;
    private static boolean regOpen;

    private int maxRounds;
    private int currentGameId;
    private List<Round> rounds;


    private Tournament() {
    }

    static {
        regOpen = true;
    }

    public static void startTournament() {
        regOpen = false;

        //TODO

        tournament = new Tournament();
        tournament.maxRounds = 1;
        tournament.currentGameId = 0;
        tournament.rounds.add(RoundManager.buildRound0());

        //Update SQL
        SQLUpdater.updateTournPlayers();
        SQLUpdater.updateTournGames();

        //TODO
    }

    static void incrementMaxRounds() {
        tournament.maxRounds++;
    }

    static int getCurrentGameId() {
        return tournament.currentGameId;
    }

    static void incrementGameId() {
        tournament.currentGameId++;
    }

    static Round getCurrentRound() {
        return tournament.rounds.get(tournament.rounds.size() - 1);
    }

    static List<Round> getRoundList() {
        return tournament.rounds;
    }
}
