package tournManager;

import constants.SQLTableNames;
import helpers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class SQLUpdater {
    private Tournament tournament;

    SQLUpdater(Tournament tournament) {
        this.tournament = tournament;
    }

    static Tournament loadTourn(int roundId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Tournament loadedTourn = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            if (roundId == -1) {
                sql = "SELECT * FROM " + SQLTableNames.SQL_CURRENT_T + " ORDER BY current_round_id DESC;";
                stmt = conn.prepareStatement(sql);
                resultSet = stmt.executeQuery();
            } else {
                sql = "SELECT * FROM " + SQLTableNames.SQL_CURRENT_T + " WHERE current_round_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, roundId);
                resultSet = stmt.executeQuery();
            }

            if (resultSet.next()) {
                loadedTourn = (Tournament) resultSet.getObject("tourn");
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
        return loadedTourn;
    }

    void saveTourn() {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            sql = "UPDATE " + SQLTableNames.SQL_CURRENT_T + " SET tourn = ? WHERE current_round_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, tournament);
            stmt.setInt(2, tournament.getCurrentRound().getRoundId());
            stmt.executeUpdate();

            stmt.close();
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

    void archiveTourn() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String dateStr = dateFormat.format(date);

            //find new tournId
            int newTournId = 0;
            while (true) {
                sql = "SELECT tourn_id from " + SQLTableNames.SQL_ARCHIVE_T + " WHERE tourn_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, newTournId);
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    newTournId++;
                } else {
                    break;
                }
            }

            //copy current_t to archive_t
            sql = "INSERT INTO " + SQLTableNames.SQL_ARCHIVE_T + " (tourn_id, date, current_round_id, tourn) SELECT ?, ?, current_round_id, tourn FROM " + SQLTableNames.SQL_CURRENT_T + ";";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, dateStr);
            stmt.setInt(2, newTournId);
            stmt.executeUpdate();

            //clear current_t
            sql = "DELETE FROM " + SQLTableNames.SQL_CURRENT_T + ";";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();

            //copy tourn_players to new table
            sql = "CREATE table ? SELECT * FROM " + SQLTableNames.SQL_TOURN_PLAYERS + ";";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "tourn_players_" + dateStr);
            stmt.executeUpdate();

            //clear tourn_players
            sql = "DELETE FROM " + SQLTableNames.SQL_TOURN_PLAYERS + ";";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();

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
    }

    void updateTournPlayers() {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            for (Player player :
                    tournament.getPlayerList()) {
                sql = "UPDATE " + SQLTableNames.SQL_TOURN_PLAYERS + " SET wins = ?, games_played = ?, byes = ?, current_game_id = ? WHERE player_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, player.getNumWins());
                stmt.setInt(2, player.getGamesPlayed());
                stmt.setInt(3, player.getNumByes());
                stmt.setInt(4, player.getCurrentGame().getGameId());
                stmt.setInt(5, player.getPlayerId());
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
                sql = "SELECT wins, games_played, byes FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, player.getPlayerId());
                resultSet = stmt.executeQuery();

                resultSet.next();
                int wins = resultSet.getInt("wins") + player.getNumWins();
                int gamesPlayed = resultSet.getInt("games_played") + player.getGamesPlayed();
                int byes = resultSet.getInt("byes") + player.getNumByes();

                sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET wins = ?, games_played = ?, byes = ? WHERE played_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, wins);
                stmt.setInt(2, gamesPlayed);
                stmt.setInt(3, byes);
                stmt.setInt(4, player.getPlayerId());
                stmt.executeUpdate();
            }

            //Update tourn_wins for the winner
            playerList.sort(new PlayerStandingsComparator());
            Player winner = playerList.get(0);

            sql = "SELECT tourn_wins FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, winner.getPlayerId());
            resultSet = stmt.executeQuery();

            resultSet.next();
            int tournWins = resultSet.getInt("tourn_wins") + 1;

            sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET tourn_wins = ? WHERE player_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tournWins);
            stmt.setInt(2, winner.getPlayerId());
            stmt.executeUpdate();

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
