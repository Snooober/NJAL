package discordbot;

import constants.BotMsgs;
import constants.DiscordIds;
import constants.SQLTableNames;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.update.GenericUserUpdateEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.managers.GuildController;
import sqlhandlers.MyDBConnection;
import sqlhandlers.PlayerLookup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static discordbot.DiscordBot.njal;
import static discordbot.DiscordBot.rossBot;

class RegistrationHandler {
    private static boolean regOpen = true;

    public static void lockReg() {
        regOpen = false;
    }

    public static void unlockReg() {
        regOpen = true;
    }

    public static synchronized void registerPlayer(MessageReceivedEvent event) {
        int discrim = Integer.parseInt(event.getAuthor().getDiscriminator());
        registerPlayer(event, event.getAuthor().getName(), event.getAuthor().getId(), discrim);
    }

    public static synchronized void registerPlayer(MessageReceivedEvent event, String discordName, String discordId, int discrim) {
        if (!regOpen) {
            event.getChannel().sendMessage(BotMsgs.regLocked).queue();
            return;
        }

        Connection conn = null;
        PreparedStatement prepSt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            //Check if discord_id is in player_info table. Update values if it is present. If not present, add to player_info.
            rs = getPlayerInfoFromDB(discordId, conn);
            if (rs.next()) {
                int player_id = rs.getInt("player_id");

                //check if steam id is linked
                rs.getString("steam_id");
                if (!(rs.wasNull())) {
                    //steam id is linked, register player
                    sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET discrim = ?, discord_name = ?, pend_reg = 0, role = 'registered' WHERE discord_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, discrim);
                    prepSt.setString(2, discordName);
                    prepSt.setString(3, discordId);
                    prepSt.executeUpdate();

                    //Check if player_id is in tourn_players table, if not add to tourn_players
                    sql = "SELECT * FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " WHERE player_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, player_id);
                    rs = prepSt.executeQuery();
                    if (rs.next()) {
                        event.getChannel().sendMessage(BotMsgs.alreadyReg(event)).queue();
                    } else {
                        //find highest order_reg
                        sql = "SELECT order_reg FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " ORDER BY order_reg DESC;";
                        rs = prepSt.executeQuery(sql);
                        rs.next();
                        int newOrderReg = rs.getInt("order_reg") + 1;

                        //insert into tourn_players
                        sql = "INSERT INTO " + SQLTableNames.SQL_TOURN_PLAYERS + " (player_id, order_reg) VALUES (?, ?);";
                        prepSt = conn.prepareStatement(sql);
                        prepSt.setInt(1, player_id);
                        prepSt.setInt(2, newOrderReg);
                        prepSt.executeUpdate();

                        //add to discord role "Registered"
                        Role regRole = njal.getRolesByName("Registered", true).iterator().next();
                        Member member = njal.getMemberById(discordId);
                        GuildController guildCont = new GuildController(njal);
                        guildCont.addSingleRoleToMember(member, regRole).queue();

                        SendMessage.sendDirectMessage(member.getUser(), BotMsgs.playerRegistered(discordName));
                        SendMessage.updateRegPlayerMsg();

                        //notify super-admin channel
                        njal.getTextChannelById(DiscordIds.ChannelIds.ROSS_LOG_CHANNEL).sendMessage(BotMsgs.playerRegistered(discordName)).queue();
                    }
                } else {
                    //steam_id not linked, pend reg
                    sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET discrim = ?, discord_name = ?, pend_reg = 1, role = NULL WHERE discord_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, discrim);
                    prepSt.setString(2, discordName);
                    prepSt.setString(3, discordId);
                    prepSt.executeUpdate();
                    sendRegQMsgs(event, discordId);
                }
            } else {
                //find empty player_id
                int newPlayerId = 0;
                while (true) {
                    sql = "SELECT player_id from " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
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
                sql = "INSERT INTO " + SQLTableNames.SQL_PLAYER_INFO + " (player_id, discord_id, discrim, discord_name) VALUES (?, ?, ?, ?);";
                prepSt = conn.prepareStatement(sql);
                prepSt.setInt(1, newPlayerId);
                prepSt.setString(2, discordId);
                prepSt.setInt(3, discrim);
                prepSt.setString(4, discordName);
                if (prepSt.executeUpdate() > 0) {
                    sendRegQMsgs(event, discordId);
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

    public static synchronized void unregisterPlayer(GuildBanEvent event) {
        unregisterPlayer(null, event.getUser().getId());
    }

    public static synchronized void unregisterPlayer(GuildMemberLeaveEvent event) {
        unregisterPlayer(null, event.getUser().getId());
    }

    public static synchronized void unregisterPlayer(MessageReceivedEvent event) {
        unregisterPlayer(event, event.getAuthor().getId());
    }

    public static synchronized void unregisterPlayer(MessageReceivedEvent event, String discordId) {
        Connection conn = null;
        PreparedStatement prepSt = null;

        User userToUnreg = rossBot.getUserById(discordId);
        int playerId = PlayerLookup.getPlayerId(discordId);
        if (playerId == -1) {
            if (event != null) {
                event.getChannel().sendMessage(BotMsgs.wasNotReg(discordId)).queue();
            }
            return;
        }
        int discrim = Integer.parseInt(userToUnreg.getDiscriminator());
        String discordName = userToUnreg.getName();

        try {
            conn = MyDBConnection.getConnection();
            ResultSet rs;
            String sql;

            removeFromTournPlayers(playerId, conn);

            //update player_info
            sql = "SELECT * FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setInt(1, playerId);
            rs = prepSt.executeQuery();
            if (rs.next()) {
                int pend_reg = rs.getInt("pend_reg");
                String role = rs.getString("role");
                if (role == null) {
                    role = "";
                }

                if (pend_reg == 1 || role.equals("registered")) {
                    sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET pend_reg = FALSE, role = null WHERE player_id = ?;";
                    prepSt = conn.prepareStatement(sql);
                    prepSt.setInt(1, playerId);
                    if (prepSt.executeUpdate() > 0) {
                        if (event != null) {
                            event.getChannel().sendMessage(BotMsgs.playerUnreg(discordName)).queue();

                            //dm the player that got unregistered, if this wasn't called by him
                            if (!(event.getAuthor().getId().equals(discordId))) {
                                User user = njal.getMemberById(discordId).getUser();
                                SendMessage.sendDirectMessage(user, BotMsgs.dmUnregPlayer);
                            }
                        } else {
                            njal.getTextChannelById(DiscordIds.ChannelIds.ROSS_LOG_CHANNEL).sendMessage(BotMsgs.playerLeftGuildAndUnreg(discordName)).queue();
                        }
                    }
                } else if (event != null) {
                    event.getChannel().sendMessage(BotMsgs.wasNotReg(discordId)).queue();
                }
            } else {
                //add to player_info, but do not register

                //find empty player_id
                int newPlayerId = 0;
                while (true) {
                    sql = "SELECT player_id from " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
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
                sql = "INSERT INTO " + SQLTableNames.SQL_PLAYER_INFO + " (player_id, discord_id, discrim, discord_name, pend_reg) VALUES (?, ?, ?, ?, 0);";
                prepSt = conn.prepareStatement(sql);
                prepSt.setInt(1, playerId);
                prepSt.setString(2, discordId);
                prepSt.setInt(3, discrim);
                prepSt.setString(4, discordName);

                if (event != null) {
                    event.getChannel().sendMessage(BotMsgs.wasNotReg(discordId)).queue();
                }
            }

            //Remove "Registered" discord role from player
            Role regRole = njal.getRolesByName("Registered", true).iterator().next();
            GuildController gc = new GuildController(njal);
            Member memberToUnreg = njal.getMemberById(discordId);
            if (memberToUnreg != null) {
                gc.removeSingleRoleFromMember(memberToUnreg, regRole).queue();
            }

            //update player-list channel
            SendMessage.updateRegPlayerMsg();

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

    public static synchronized void unregisterAllPlayers() {
        Connection conn = null;
        PreparedStatement prepSt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            //tourn_players
            sql = "DELETE FROM " + SQLTableNames.SQL_TOURN_PLAYERS + ";";
            prepSt = conn.prepareStatement(sql);
            prepSt.executeUpdate();

            //player_info
            sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET pend_reg = false, role = null;";
            prepSt.executeUpdate(sql);

            //make blank copy of "Registered" role effectively removing all members from the role
            GuildController gc = new GuildController(njal);
            Role reg = njal.getRolesByName("Registered", true).iterator().next();
            gc.createCopyOfRole(reg).queue();
            reg.delete().queue();

            //update player-list channel
            SendMessage.updateRegPlayerMsg();

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

    private static ResultSet getPlayerInfoFromDB(String discordId, Connection conn) throws SQLException {
        PreparedStatement prepSt;
        String sql;
        ResultSet rs;

        sql = "SELECT * FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE discord_id = ?;";
        prepSt = conn.prepareStatement(sql);
        prepSt.setString(1, discordId);
        rs = prepSt.executeQuery();

        prepSt.close();

        return rs;
    }

    public static synchronized void updatePlayerInfo(Event event) {
        Connection conn = null;
        PreparedStatement prepSt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            User user;
            if (event instanceof GuildMemberJoinEvent) {
                user = ((GuildMemberJoinEvent) event).getUser();
            } else {
                user = ((GenericUserUpdateEvent) event).getUser();
            }

            String discordId = user.getId();
            int discrim = Integer.parseInt(user.getDiscriminator());
            String discordName = user.getName();

            //check if discord_id is in player_info table
            rs = getPlayerInfoFromDB(discordId, conn);
            if (rs.next()) {
                //notify ross-log
                int oldDiscrim = rs.getInt("discrim");
                String oldDiscordName = rs.getString("discord_name");
                if (event instanceof GuildMemberJoinEvent) {
                    njal.getTextChannelById(DiscordIds.ChannelIds.ROSS_LOG_CHANNEL).sendMessage(BotMsgs.memberJoinNewInfo(oldDiscordName, oldDiscrim, discordName, discrim)).queue();
                } else if (event instanceof UserUpdateNameEvent) {
                    njal.getTextChannelById(DiscordIds.ChannelIds.ROSS_LOG_CHANNEL).sendMessage(BotMsgs.userUpdatedName(((UserUpdateNameEvent) event).getOldName(), discordName)).queue();
                } else if (event instanceof UserUpdateDiscriminatorEvent) {
                    njal.getTextChannelById(DiscordIds.ChannelIds.ROSS_LOG_CHANNEL).sendMessage(BotMsgs.userUpdatedDiscrim((((UserUpdateDiscriminatorEvent) event).getOldDiscriminator()), discrim)).queue();
                }

                //update player_info
                sql = "UPDATE " + SQLTableNames.SQL_PLAYER_INFO + " SET discrim = ?, discord_name = ?;";
                prepSt = conn.prepareStatement(sql);
                prepSt.setInt(1, discrim);
                prepSt.setString(2, discordName);
                prepSt.executeUpdate();
            }

            rs.close();
            if (prepSt != null) {
                prepSt.close();
            }
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

    private static void removeFromTournPlayers(int playerId, Connection conn) throws SQLException {
        String sql;
        PreparedStatement prepSt;
        ResultSet rs;

        sql = "SELECT * FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " WHERE player_id = ?;";
        prepSt = conn.prepareStatement(sql);
        prepSt.setInt(1, playerId);
        rs = prepSt.executeQuery();
        if (rs.next()) {
            int orderReg = rs.getInt("order_reg");
            sql = "DELETE FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " WHERE player_id = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setInt(1, playerId);
            prepSt.execute();

            //adjust the higher order_reg's
            sql = "SELECT order_reg FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " WHERE order_reg = ?;";
            prepSt = conn.prepareStatement(sql);
            prepSt.setInt(1, (orderReg + 1));
            rs = prepSt.executeQuery();
            while (rs.next()) {
                sql = "UPDATE " + SQLTableNames.SQL_TOURN_PLAYERS + " SET order_reg = ? WHERE order_reg = ?;";
                prepSt = conn.prepareStatement(sql);
                prepSt.setInt(1, orderReg);
                prepSt.setInt(2, orderReg + 1);
                prepSt.executeUpdate();

                orderReg++;
                sql = "SELECT order_reg FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " WHERE order_reg = ?;";
                prepSt = conn.prepareStatement(sql);
                prepSt.setInt(1, (orderReg + 1));
                rs = prepSt.executeQuery();
            }
        }

        rs.close();
        prepSt.close();
    }

    private static void sendRegQMsgs(MessageReceivedEvent event, String discordId) {
        //send channel message
        if (!event.getMessage().getChannelType().equals(ChannelType.PRIVATE)) {
            event.getChannel().sendMessage(BotMsgs.regQueueChan(event)).queue();
        }
        //send direct message
        User user = njal.getMemberById(discordId).getUser();
        SendMessage.sendDirectMessage(user, BotMsgs.regQueueDM(user.getName()));
        //notify ross-log
        njal.getTextChannelById(DiscordIds.ChannelIds.ROSS_LOG_CHANNEL).sendMessage(BotMsgs.regQueueSuperAdmin(user.getName())).queue();
    }
}