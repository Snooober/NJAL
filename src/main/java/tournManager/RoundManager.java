package tournManager;

import constants.SQLTableNames;
import sqlHandlers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundManager {

    static Round buildRound0() {
        List<Integer> playerIdList = getTournPlayerIds();

        Round round0 = new Round(0);

        //Determine max rounds
        int timesTwo = 2;
        while (timesTwo < playerIdList.size()) {
            timesTwo = timesTwo * 2;
            Tournament.incrementMaxRounds();
        }

        //Shuffle player list
        Collections.shuffle(playerIdList);

        //Add 2 players to new game. Add game to round.
        int index = 0;
        while (index < playerIdList.size() - 1) {
            Player player1 = new Player(playerIdList.get(index));
            Player player2 = new Player(playerIdList.get(index + 1));
            Game newGame = new Game(player1, player2);
            round0.addGame(newGame);

            index = index + 2;
        }

        //Manage bye
        if (index==(playerIdList.size())-1) {
            round0.setByePlayer(new Player(playerIdList.get(index)));
        }

        return round0;
    }

    private static List<Integer> getTournPlayerIds() {
        List<Integer> playerIds = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            sql = "SELECT player_id FROM ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_TOURN_PLAYERS);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                playerIds.add(resultSet.getInt("player_id"));
            }

            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return playerIds;
    }
}
