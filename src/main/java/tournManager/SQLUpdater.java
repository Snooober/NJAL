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
                sql = "SELECT * FROM ? ORDER BY current_round_id DESC;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_CURRENT_T);
                resultSet = stmt.executeQuery();
            } else {
                sql = "SELECT * FROM ? WHERE current_round_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_CURRENT_T);
                stmt.setInt(2, roundId);
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

            sql = "UPDATE ? SET tourn = ? WHERE current_round_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_CURRENT_T);
            stmt.setObject(2, tournament);
            stmt.setInt(3, tournament.getCurrentRound().getRoundId());
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
                sql = "SELECT tourn_id from ? WHERE tourn_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, SQLTableNames.SQL_ARCHIVE_T);
                stmt.setInt(2, newTournId);
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    newTournId++;
                } else {
                    break;
                }
            }

            //copy current_t to archive_t
            sql = "INSERT INTO ? (tourn_id, date, current_round_id, tourn) SELECT ?, ?, current_round_id, tourn FROM ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_ARCHIVE_T);
            stmt.setString(2, dateStr);
            stmt.setInt(3, newTournId);
            stmt.setString(4, SQLTableNames.SQL_CURRENT_T);
            stmt.executeUpdate();

            //clear current_t
            sql = "DELETE FROM ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_CURRENT_T);
            stmt.executeUpdate();

            //copy tourn_players to new table
            sql = "CREATE table ? SELECT * FROM ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "tourn_players_" + dateStr);
            stmt.setString(2, SQLTableNames.SQL_TOURN_PLAYERS);
            stmt.executeUpdate();

            //clear tourn_players
            sql = "DELETE FROM ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_TOURN_PLAYERS);
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
            Player winner = playerList.get(0);

            sql = "SELECT tourn_wins FROM ? WHERE player_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_PLAYER_INFO);
            stmt.setInt(2, winner.getPlayerId());
            resultSet = stmt.executeQuery();

            resultSet.next();
            int tournWins = resultSet.getInt("tourn_wins") + 1;

            sql = "UPDATE ? SET tourn_wins = ? WHERE player_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_PLAYER_INFO);
            stmt.setInt(2, tournWins);
            stmt.setInt(3, winner.getPlayerId());
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
