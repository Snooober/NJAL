package tournManager;

import discordBot.SendMessage;

import java.util.ArrayList;
import java.util.List;

public class Tournament {
    private static Tournament tournament;

    private int maxRounds;
    private int currentGameId;
    private List<Player> playerList;
    private RoundManager roundManager;
    private SQLUpdater sqlUpdater;

    private Tournament() {
    }

    public static void newTournament() {
        //TODO archive tournament and clear SQL tables

        tournament = new Tournament();
        tournament.initTournament();
    }

    private void initTournament() {
        roundManager = new RoundManager(this);
        sqlUpdater = new SQLUpdater(this);
        playerList = PlayerListBuilder.getPlayerList();
        initMaxRounds();
        currentGameId = 0;

        roundManager.addRound0();
        sqlUpdater.update();
        //TODO save tournament

        SendMessage.sendStandings(this);
    }

    private void initMaxRounds() {
        maxRounds = 1;
        int timesTwo = 2;
        while (timesTwo < playerList.size()) {
            timesTwo = timesTwo * 2;
            maxRounds++;
        }
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

    public List<Player> getPlayerList() {
        return playerList;
    }
}
