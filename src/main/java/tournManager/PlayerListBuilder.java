package tournManager;

import constants.SQLTableNames;
import helpers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerListBuilder {

    static Map<Integer, Player> getPlayerMap() {
        Map<Integer, Player> playerMap = new HashMap<>();

        for (Integer playerId :
                getTournPlayerIds()) {
            Player player = new Player(playerId);
            playerMap.put(player.getPlayerId(), player);
        }

        return playerMap;
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
