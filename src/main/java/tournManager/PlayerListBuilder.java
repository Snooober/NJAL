package tournManager;

import constants.SQLTableNames;
import helpers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerListBuilder {

    static List<Player> getPlayerList() {
        List<Player> playerList = new ArrayList<>();
        Iterator<Integer> playerIds = getTournPlayerIds().iterator();
        while (playerIds.hasNext()) {
            Player player = new Player(playerIds.next());
            playerList.add(player);
        }

        return playerList;
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
