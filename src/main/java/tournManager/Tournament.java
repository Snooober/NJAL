package tournManager;

import discordBot.RegistrationHandler;
import discordBot.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tournament {
    private static Tournament tournament;

    private int maxRounds;
    private int currentGameId;
    private Map<Integer, Player> playerMap;
    private RoundManager roundManager;
    private SQLUpdater sqlUpdater;

    private Tournament() {
    }

    public static void newTournament() {
        RegistrationHandler.lockReg();

        //TODO archive tournament and clear SQL tables

        tournament = new Tournament();
        tournament.initTournament();
    }

    public static Tournament getOnGoingTourn() {
        return tournament;
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
        sqlUpdater.update();
        //TODO save tournament
    }

    private void initMaxRounds() {
        maxRounds = 1;
        int timesTwo = 2;
        while (timesTwo < playerMap.size()) {
            timesTwo = timesTwo * 2;
            maxRounds++;
        }
    }

    void updateSQL() {
        sqlUpdater.update();
    }

    void updateOverallStats() {
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
        return (!(roundManager.getRoundsList().size() < (maxRounds - 1)));
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
