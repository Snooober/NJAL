package tournManager;

import com.mysql.jdbc.MySQLConnection;
import constants.SQLTableNames;
import helpers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class SQLUpdater {
    private Tournament tournament;

    SQLUpdater(Tournament tournament) {
        this.tournament = tournament;
    }

    void update() {
        updateTournPlayers();
        updateTournGames();
    }

    private void updateTournPlayers() {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            for (Player player :
                    tournament.getPlayerList()) {
                sql = "UPDATE ? SET wins = ?, games_played = ?, byes = ?, current_game_id = ? WHERE player_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_TOURN_PLAYERS);
                stmt.setInt(2, player.getNumWins());
                stmt.setInt(3, player.getGamesPlayed());
                stmt.setInt(4, player.getNumByes());
                stmt.setInt(5, player.getCurrentGame().getGameId());
                stmt.setInt(6, player.getPlayerId());
                stmt.executeUpdate();
            }

            if (stmt != null) {
                stmt.close();
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
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
    }

    //TODO remove game_round_id from SQL table
    //TODO change comp1 to player1 in SQL tables
    private void updateTournGames() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            for (Game game :
                    tournament.getGamesList()) {

                int gameId = game.getGameId();
                int roundId = game.getRound().getRoundId();
                int player1Id = game.getPlayer1().getPlayerId();
                int player2Id = game.getPlayer2().getPlayerId();
                WinStatus player1Report = game.getPlayer1Report();
                WinStatus player2Report = game.getPlayer2Report();
                WinStatus winStatus = game.getWinStatus();

                //Check if current gameId exists
                sql = "SELECT game_id from ? WHERE game_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_TOURN_GAMES);
                stmt.setInt(2, gameId);
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    sql = "UPDATE ? SET game_id = ?, round = ?, player1_id = ?, player2_id = ?, player1_report = ?, player2_report =?, win_status = ? WHERE game_id = ?;";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, SQLTableNames.SQL_TOURN_GAMES);
                    stmt.setInt(2, gameId);
                    stmt.setInt(3, roundId);
                    stmt.setInt(4, player1Id);
                    stmt.setInt(5, player2Id);
                    stmt.setString(6, player1Report.name());
                    stmt.setString(7, player2Report.name());
                    stmt.setString(8, winStatus.name());
                    stmt.setInt(9, gameId);
                    stmt.executeUpdate();
                } else {
                    sql = "INSERT INTO ? (game_id, round, player1_id, player2_id, player1_report, player2_report, win_status) VALUES (?, ?, ?, ?, ?, ?, ?);";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, SQLTableNames.SQL_TOURN_GAMES);
                    stmt.setInt(2, gameId);
                    stmt.setInt(3, roundId);
                    stmt.setInt(4, player1Id);
                    stmt.setInt(5, player2Id);
                    stmt.setString(6, player1Report.name());
                    stmt.setString(7, player2Report.name());
                    stmt.setString(8, winStatus.name());
                    stmt.executeUpdate();
                }
            }

            if (resultSet != null) {
                resultSet.close();
            }
            if (stmt != null) {
                stmt.close();
            }
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
    }

    void updateOverallStats() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            List<Player> playerList = tournament.getPlayerList();
            for (Player player :
                    playerList) {

                //get wins, games played, byes from player
                sql = "SELECT wins, games_played, byes FROM ? WHERE player_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_PLAYER_INFO);
                stmt.setInt(2, player.getPlayerId());
                resultSet = stmt.executeQuery();

                resultSet.next();
                int wins = resultSet.getInt("wins") + player.getNumWins();
                int gamesPlayed = resultSet.getInt("games_played") + player.getGamesPlayed();
                int byes = resultSet.getInt("byes") + player.getNumByes();

                sql = "UPDATE ? SET wins = ?, games_played = ?, byes = ? WHERE played_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_PLAYER_INFO);
                stmt.setInt(2, wins);
                stmt.setInt(3, gamesPlayed);
                stmt.setInt(4, byes);
                stmt.setInt(5, player.getPlayerId());
                stmt.executeUpdate();
            }

            //Update tourn_wins for the winner
            playerList.sort(new PlayerStandingsComparator());


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
    }
}
