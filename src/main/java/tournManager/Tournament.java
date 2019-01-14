package tournManager;

import java.util.List;

public class Tournament {
    private static Tournament tournament;
    private static boolean regOpen;

    private int maxRounds;
    private int currentGameId;
    private List<Round> rounds;


    private Tournament(){}

    static {
        regOpen = true;
    }

    public static Tournament startTournament() {
        regOpen = false;

        //TODO

        tournament = new Tournament();
        tournament.maxRounds = 1;
        tournament.currentGameId = 0;
        tournament.rounds.add(RoundManager.buildRound0());

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
        return tournament.rounds.get(tournament.rounds.size()-1);
    }

    static List<Round> getRoundList() {
        return tournament.rounds;
    }
}
