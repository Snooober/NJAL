package discordBot;

import constants.BotMsgs;
import constants.SQLTableNames;
import helpers.MyDBConnection;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static discordBot.DiscordBot.njal;

class SteamConnector {
    public static void connectSteamId(MessageReceivedEvent event, String discordId, String steamId) {
        Connection conn = null;
        PreparedStatement prepSt = null;

        try {
            conn = MyDBConnection.getConnection();
            ResultSet rs;
            ResultSet rsSteamId;
            String sql;

            //check if discord ID is found, and that steam_id is NULL
            sql = "SELECT discord_id, discrim, steam_id, discord_name, pend_reg FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE discord_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setString(1, discordId);
            rs = prepSt.executeQuery();
            if (!rs.next()) {
                event.getChannel().sendMessage(BotMsgs.discordIdNotFound).queue();
                return;
            }
            if (rs.getString("steam_id") != null) {
                event.getChannel().sendMessage(BotMsgs.discordIdhasSteamId).queue();
                return;
            }

            //check if steam ID is already linked to another discord account
            sql = "SELECT steam_id FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE steam_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setString(1, steamId);
            rsSteamId = prepSt.executeQuery();
            if (rsSteamId.next()) {
                //steam id already linked to a discord account
                event.getChannel().sendMessage(BotMsgs.steamIdAlreadyLinked).queue();
                return;
            }

            //link steam ID
            int discrim = rs.getInt("discrim");
            String discordName = rs.getString("discord_name");
            sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET steam_id = ? WHERE discord_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setString(1, steamId);
            prepSt.setString(2, discordId);
            int update = prepSt.executeUpdate();
            if (update == 1) {
                //check if pending registration
                boolean pendReg = rs.getBoolean("pend_reg");
                if (pendReg) {
                    event.getChannel().sendMessage(BotMsgs.steamIdLinkedAttemptReg).queue();
                    RegistrationHandler.registerPlayer(event, discordName, discordId, discrim);
                    njal.getMemberById(discordId).getUser().openPrivateChannel().complete().sendMessage(BotMsgs.steamConnectedAndRegDM).queue();
                } else {
                    event.getChannel().sendMessage(BotMsgs.steamIdLinkedNotPendReg).queue();
                }
            } else {
                if (update > 1) {
                    event.getChannel().sendMessage(BotMsgs.steamIdLinkedMultiple).queue();
                } else {
                    event.getChannel().sendMessage(BotMsgs.steamIdProblemLinking).queue();
                }
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
    }

    public static void sendPendRegPlayers(MessageReceivedEvent event) {
        Connection conn = null;
        PreparedStatement prepSt = null;

        try {
            conn = MyDBConnection.getConnection();
            ResultSet rs;
            String sql;

            List<String> discordNameCol = new ArrayList<>();
            List<String> discrimCol = new ArrayList<>();
            List<String> discordIdCol = new ArrayList<>();
            List<String> steamIdCol = new ArrayList<>();

            discordNameCol.add("Discord Name");
            discrimCol.add("Discriminator");
            discordIdCol.add("Discord ID");
            steamIdCol.add("Steam ID");

            String discordName;
            Integer discrim;
            String discordId;
            String steamId;

            sql = "SELECT discord_id, discrim, steam_id, discord_name FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE pend_reg = true;";
            prepSt = conn.prepareStatement(sql);
            rs = prepSt.executeQuery();
            while (rs.next()) {
                discordName = rs.getString("discord_name");
                discrim = rs.getInt("discrim");
                discordId = rs.getString("discord_id");
                steamId = rs.getString("steam_id");
                if (steamId == null) {
                    steamId = "";
                }

                discordNameCol.add(discordName);
                discrimCol.add(discrim.toString());
                discordIdCol.add(discordId);
                steamIdCol.add(steamId);
            }

            List<List<String>> columnsList = new ArrayList<>();
            columnsList.add(discordNameCol);
            columnsList.add(discrimCol);
            columnsList.add(discordIdCol);
            columnsList.add(steamIdCol);

            List<String> message = SendMessage.listMessageBuilder(columnsList);
            for (String s : message) {
                event.getChannel().sendMessage(s).queue();
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
    }
}
