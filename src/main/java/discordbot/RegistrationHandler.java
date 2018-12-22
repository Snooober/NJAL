package discordbot;

import constants.BotMsgs;
import constants.DiscordIds;
import constants.SQL_TableNames;
import net.dv8tion.jda.core.entities.ChannelType;
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
        PreparedStatement prepSt = null;

        String discordName = event.getAuthor().getName();
        String discordId = event.getAuthor().getId();
        Integer discrim = Integer.parseInt(event.getAuthor().getDiscriminator());

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            //Check if discord_id is in player_info table. Update values if it is present. If not present, add to player_info.
            sql = "SELECT * FROM " + SQL_TableNames.SQL_PLAYER_INFO + " WHERE discord_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setString(1, discordId);
            rs = prepSt.executeQuery();
            if (rs.next()) {
                int player_id = rs.getInt("player_id");

                //check if steam id is linked
                rs.getString("steam_id");
                if (!(rs.wasNull())) {
                    //steam id is linked, register player
                    sql = "UPDATE " + SQL_TableNames.SQL_PLAYER_INFO + " SET discrim = ?, discord_name = ?, pend_reg = 0, role = 'registered' WHERE discord_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, discrim);
                    prepSt.setString(2, discordName);
                    prepSt.setString(3, discordId);
                    if (prepSt.executeUpdate() < 0) {
                        //TODO report problem and break
                    }

                    //Check if player_id is in tourn_players table, if not add to tourn_players
                    sql = "SELECT * FROM " + SQL_TableNames.SQL_TOURN_PLAYERS + " WHERE player_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, player_id);
                    rs = prepSt.executeQuery();
                    if (rs.next()) {
                        event.getChannel().sendMessage(event.getAuthor().getName() + " was already registered.").queue();
                    } else {
                        //find highest order_reg
                        sql = "SELECT order_reg FROM " + SQL_TableNames.SQL_TOURN_PLAYERS + " ORDER BY order_reg DESC;";
                        rs = prepSt.executeQuery(sql);
                        rs.next();
                        int newOrderReg = rs.getInt("order_reg") + 1;

                        //insert into tourn_players
                        sql = "INSERT INTO " + SQL_TableNames.SQL_TOURN_PLAYERS + " (player_id, order_reg) VALUES (?, ?);";
                        prepSt = conn.prepareStatement(sql);
                        prepSt.setInt(1, player_id);
                        prepSt.setInt(2, newOrderReg);
                        if (prepSt.executeUpdate() > 0) {
                            Role regRole = DiscordBot.njal.getRolesByName("Registered", true).iterator().next();
                            Member member = DiscordBot.njal.getMember(event.getAuthor());
                            GuildController guildCont = new GuildController(DiscordBot.njal);
                            guildCont.addSingleRoleToMember(member, regRole).queue();

                            event.getChannel().sendMessage(event.getAuthor().getName() + " has been registered!").queue();
                            SendMessage.updateRegPlayerMsg();

                            //notify super-admin channel
                            DiscordBot.njal.getTextChannelById(DiscordIds.ChannelIds.SUPER_ADMIN_CHANNEL).sendMessage(event.getAuthor().getName() + " has registered.").queue();
                        } else {
                            //TODO report problem
                        }
                    }
                } else {
                    //steam_id not linked, pend reg
                    sql = "UPDATE " + SQL_TableNames.SQL_PLAYER_INFO + " SET discrim = ?, discord_name = ?, pend_reg = 1, role = NULL WHERE discord_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, discrim);
                    prepSt.setString(2, discordName);
                    prepSt.setString(3, discordId);
                    if (prepSt.executeUpdate() > 0) {
                        sendRegQMsgs(event);
                    } else {
                        //TODO report problem
                    }
                }
            } else {
                //find empty player_id
                int newPlayerId = 0;
                while (true) {
                    sql = "SELECT player_id from " + SQL_TableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, newPlayerId);
                    rs = prepSt.executeQuery();
                    if (rs.next()) {
                        newPlayerId++;
                    } else {
                        break;
                    }
                }

                //insert new player into player_info
                sql = "INSERT INTO " + SQL_TableNames.SQL_PLAYER_INFO + " (player_id, discord_id, discrim, discord_name) VALUES (?, ?, ?, ?);";
                prepSt = conn.prepareStatement(sql);
                prepSt.setInt(1, newPlayerId);
                prepSt.setString(2, discordId);
                prepSt.setInt(3, discrim);
                prepSt.setString(4, discordName);
                if (prepSt.executeUpdate() > 0) {
                    sendRegQMsgs(event);
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

    private static void sendRegQMsgs(MessageReceivedEvent event) {
        //send channel message
        if (!event.getMessage().getChannelType().equals(ChannelType.PRIVATE)) {
            event.getChannel().sendMessage(BotMsgs.regQueueChan(event)).queue();
        }
        //send direct message
        event.getAuthor().openPrivateChannel().complete().sendMessage(BotMsgs.regQueueDM(event)).queue();
        //notify super-admin
        DiscordBot.njal.getTextChannelById(DiscordIds.ChannelIds.SUPER_ADMIN_CHANNEL).sendMessage(event.getAuthor().getName() + " is pending registration.").queue();
    }
}
