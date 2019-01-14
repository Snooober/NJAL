package helpers;

import constants.SQLTableNames;
import helpers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerLookup {
    public static int getPlayerId(String discordId) {
        Connection conn = null;
        PreparedStatement prepSt = null;
        int player_id = 0;

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            sql = "SELECT player_id FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE discord_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setString(1, discordId);
            rs = prepSt.executeQuery();
            if (rs.next()) {
                player_id = rs.getInt("player_id");
            } else {
                player_id = -1;
            }

            rs.close();
            prepSt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (prepSt != null) {
                    prepSt.close();
                }
            } catch (SQLException se) {
                //do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return player_id;
    }

    public static String getDiscordId(int playerId) {
        Connection conn = null;
        PreparedStatement prepSt = null;
        String discordId = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            sql = "SELECT discord_id FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setInt(1, playerId);
            rs = prepSt.executeQuery();
            if (rs.next()) {
                discordId = rs.getString("discord_id");
            }

            rs.close();
            prepSt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (prepSt != null) {
                    prepSt.close();
                }
            } catch (SQLException se) {
                //do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return discordId;
    }

    public static String getDiscordName(String discordId) {
        Connection conn = null;
        PreparedStatement prepSt = null;
        String discordName = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            sql = "SELECT discord_name FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE discord_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setString(1, discordId);
            rs = prepSt.executeQuery();
            if (rs.next()) {
                discordName = rs.getString("discord_name");
            }

            rs.close();
            prepSt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (prepSt != null) {
                    prepSt.close();
                }
            } catch (SQLException se) {
                //do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return discordName;

    }
}
