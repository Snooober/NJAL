package discordbot;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;
import sqlhandlers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationHandler {
    private static void registerPlayer(MessageReceivedEvent event) {
        Connection conn = null;
        PreparedStatement stmt = null;

        String discordName = event.getAuthor().getName();
        String discordId = event.getAuthor().getId();
        Integer discrim = Integer.parseInt(event.getAuthor().getDiscriminator());

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            //Check if discord_id is in player_info table. Update values if it is present. If not present, add to player_info.
            sql = "SELECT * FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE discord_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, discordId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int player_id = rs.getInt("player_id");

                //check if steam id is linked
                rs.getString("steam_id");
                if (!(rs.wasNull())) {
                    //steam id is linked, register player
                    sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET discrim = ?, discord_name = ?, pend_reg = 0, role = 'registered' WHERE discord_id = ?;";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, discrim);
                    stmt.setString(2, discordName);
                    stmt.setString(3, discordId);
                    stmt.execute();

                    //Check if player_id is in tourn_players table, if not add to tourn_players
                    sql = "SELECT * FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " WHERE player_id = ?;";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, player_id);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        event.getChannel().sendMessage(event.getAuthor().getName() + " was already registered.").queue();
                    } else {
                        sql = "SELECT order_reg FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " ORDER BY order_reg ASC;";
                        rs = stmt.executeQuery(sql);
                        int newOrderReg = 1;
                        while (rs.next()) {
                            int currentOrderReg = rs.getInt("order_reg");
                            if (currentOrderReg >= newOrderReg) {
                                newOrderReg = currentOrderReg + 1;
                            }
                        }

                        sql = "INSERT INTO " + SQLTableNames.SQL_TOURN_PLAYERS + " (player_id, order_reg) VALUES ('" + player_id + "', '" + newOrderReg + "');";
                        int rowsUpdated = stmt.executeUpdate(sql);
                        if (rowsUpdated > 0) {
                            Guild njal = DiscordBot.discordBot.getGuildById(ConfigConstants.NJAL_GUILD_ID);
                            Role regRole = njal.getRolesByName("Registered", true).iterator().next();
                            GuildController gc = new GuildController(njal);
                            Member member = njal.getMember(event.getAuthor());
                            gc.addSingleRoleToMember(member, regRole).queue();

                            event.getChannel().sendMessage(event.getAuthor().getName() + " has been registered!").queue();
                            SendMessage.editRegPlayerMsg();

                            //notify me
                            DiscordBot.discordBot.getGuildById(ConfigConstants.NJAL_GUILD_ID).getTextChannelById(ConfigConstants.SUPER_ADMIN_CHANNEL).sendMessage(event.getAuthor().getName() + " has registered.").queue();
                        }
                    }
                } else {
                    //steam_id not linked, pend reg
                    sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET discrim = ?, discord_name = ?, pend_reg = 1, role = NULL WHERE discord_id = ?;";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, discrim);
                    stmt.setString(2, discordName);
                    stmt.setString(3, discordId);
                    stmt.executeUpdate();

                    if (!event.getMessage().getChannelType().equals(ChannelType.PRIVATE)) {
                        event.getChannel().sendMessage(regQChannel).queue();
                    }
                    event.getAuthor().openPrivateChannel().complete().sendMessage(regQ).queue();

                    //notify me
                    DiscordBot.discordBot.getGuildById(ConfigConstants.NJAL_GUILD_ID).getTextChannelById(ConfigConstants.SUPER_ADMIN_CHANNEL).sendMessage(event.getAuthor().getName() + " is pending registration.").queue();
                }
            } else {
                //find empty player_id
                int newPlayerId = 0;
                while (true) {
                    sql = "SELECT player_id from " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = " + newPlayerId + ";";
                    rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        newPlayerId++;
                    } else {
                        break;
                    }
                }

                //insert new player into player_info
                sql = "INSERT INTO " + SQLTableNames.SQL_PLAYER_INFO + " (player_id, discord_id, discrim, discord_name) VALUES (?, ?, ?, ?);";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, newPlayerId);
                stmt.setString(2, discordId);
                stmt.setInt(3, discrim);
                stmt.setString(4, discordName);
                stmt.executeUpdate();

                if (!event.getMessage().getChannelType().equals(ChannelType.PRIVATE)) {
                    event.getChannel().sendMessage(regQChannel).queue();
                }
                event.getAuthor().openPrivateChannel().complete().sendMessage(regQ).queue();

                //notify me
                DiscordBot.discordBot.getGuildById(ConfigConstants.NJAL_GUILD_ID).getTextChannelById(ConfigConstants.SUPER_ADMIN_CHANNEL).sendMessage(event.getAuthor().getName() + " is pending registration.").queue();
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
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
