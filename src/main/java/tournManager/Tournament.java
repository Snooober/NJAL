package tournManager;

import discordBot.RegistrationHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tournament implements Serializable {
    private static Tournament tournament = null;

    private int maxRounds;
    private int currentGameId;
    private Map<Integer, Player> playerMap;
    private RoundManager roundManager;
    private SQLUpdater sqlUpdater;

    private Tournament() {
    }

    public static void newTournament() {
        RegistrationHandler.lockReg();

        //if another tournament exists, archive it and clear SQL tables
        if (tournament != null) {
            tournament.archiveTourn();
        }

        tournament = new Tournament();
        tournament.initTournament();
    }

    public static boolean loadTournament(int roundId) {
        RegistrationHandler.lockReg();
        Tournament loadedTourn = SQLUpdater.loadTourn(roundId);
        if (loadedTourn != null) {
            tournament = loadedTourn;
            return true;
        } else {
            return false;
        }
    }

    public static Tournament getOnGoingTourn() {
        return tournament;
    }

    static void endTourn() {
        tournament = null;
    }

    void checkNextRound() {
        roundManager.checkNextRound();
    }

    private void initTournament() {
        roundManager = new RoundManager(this);
        sqlUpdater = new SQLUpdater(this);
        playerMap = PlayerListBuilder.getPlayerMap();
        initMaxRounds();
        currentGameId = 0;

        roundManager.addRound();
        sqlUpdater.updateTournPlayers();
    }

    private void initMaxRounds() {
        maxRounds = 1;
        int timesTwo = 2;
        while (timesTwo < playerMap.size()) {
            timesTwo = timesTwo * 2;
            maxRounds++;
        }
    }

    void saveTourn() {
        sqlUpdater.saveTourn();
    }

    void archiveTourn() {
        sqlUpdater.saveTourn();
        sqlUpdater.archiveTourn();
    }

    void updateSQLtournPlayers() {
        sqlUpdater.updateTournPlayers();
    }

    void updateOverallStatsSQL() {
        sqlUpdater.updateOverallStats();

    }

    int getCurrentGameId() {
        return currentGameId;
    }

    void incrementGameId() {
        currentGameId++;
    }

    public Round getCurrentRound() {
        List<Round> roundsList = roundManager.getRoundsList();
        return roundsList.get(roundsList.size() - 1);
    }

    public boolean onFinalRound() {
        return (!(roundManager.getRoundsList().size() < maxRounds));
    }

    List<Round> getRoundsList() {
        return roundManager.getRoundsList();
    }

    List<Game> getGamesList() {
        List<Game> gamesList = new ArrayList<>();

        for (Round round :
                roundManager.getRoundsList()) {
            gamesList.addAll(round.getRoundGames());
        }

        return gamesList;
    }

    public Map<Integer, Player> getPlayerMap() {
        return playerMap;
    }

    public List<Player> getPlayerList() {
        return new ArrayList<>(playerMap.values());
    }
}
