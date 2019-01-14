package tournManager;

import constants.SQLTableNames;
import sqlHandlers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class SQLUpdater {

    //TODO should update by using a playerList member variable in Tournament()
    static void updateTournPlayers() {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            Round currentRound = Tournament.getCurrentRound();
            Iterator<Game> roundGamesIt = currentRound.getRoundGames().iterator();
            while (roundGamesIt.hasNext()) {
                Game currentGame = roundGamesIt.next();
                int currentGameId = currentGame.getGameId();
                Player player1 = currentGame.getPlayer1();
                Player player2 = currentGame.getPlayer2();
                int player1Id = player1.getPlayerId();
                int player1Wins = player1.getNumWins();
                int player1GamesPlayed = player1.getGamesPlayed();
                int player1Byes = player1.getNumByes();
                int player2Id = player2.getPlayerId();
                int player2Wins = player2.getNumWins();
                int player2GamesPlayed = player2.getGamesPlayed();
                int player2Byes = player2.getNumByes();

                //update records for player1
                sql = "UPDATE ? SET wins = ?, games_played = ?, byes = ?, current_game_id = ? WHERE player_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_TOURN_PLAYERS);
                stmt.setInt(2, player1Wins);
                stmt.setInt(3, player1GamesPlayed);
                stmt.setInt(4, player1Byes);
                stmt.setInt(5, currentGameId);
                stmt.setInt(6, player1Id);
                stmt.executeUpdate();

                //update records for player2
                sql = "UPDATE ? SET wins = ?, games_played = ?, byes = ?, current_game_id = ? WHERE player_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_TOURN_PLAYERS);
                stmt.setInt(2, player2Wins);
                stmt.setInt(3, player2GamesPlayed);
                stmt.setInt(4, player2Byes);
                stmt.setInt(5, currentGameId);
                stmt.setInt(6, player2Id);
                stmt.executeUpdate();
            }

            //update values for bye player
            Player byePlayer = currentRound.getByePlayer();
            if (byePlayer != null) {
                sql = "UPDATE ? SET wins = ?, games_played = ?, byes = ?, current_game_id = null WHERE player_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_TOURN_PLAYERS);
                stmt.setInt(2, byePlayer.getNumWins());
                stmt.setInt(3, byePlayer.getGamesPlayed());
                stmt.setInt(4, byePlayer.getNumByes());
                stmt.setInt(5, byePlayer.getPlayerId());
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
    static void updateTournGames() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            Iterator<Round> roundIt = Tournament.getRoundList().iterator();
            while (roundIt.hasNext()) {
                Iterator<Game> gamesIt = roundIt.next().getRoundGames().iterator();
                while (gamesIt.hasNext()) {
                    Game game = gamesIt.next();

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
}
