package discordbot;

import constants.BotMsgs;
import constants.DiscordIds;
import constants.SQLTableNames;
import helpers.CSVHelper;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sqlhandlers.MyDBConnection;
import sqlhandlers.PlayerLookup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static discordbot.DiscordBot.njal;

class SendMessage {
    public static void sendDirectMessage(User user, String message) {
        user.openPrivateChannel().complete().sendMessage(message).queue();
        System.out.println("Sent direct message to " + user.getName() + ": " + message);
    }

    public static void directMsgRegPlayers(String message) {
        Connection conn = null;
        PreparedStatement prepSt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            sql = "SELECT discord_id, role FROM " + SQLTableNames.SQL_PLAYER_INFO + ";";
            prepSt = conn.prepareStatement(sql);
            rs = prepSt.executeQuery();

            while (rs.next()) {
                String discordId = rs.getString("discord_id");
                String role = rs.getString("role");

                if (role != null) {
                    if (role.equals("registered")) {
                        User user = njal.getMemberById(discordId).getUser();
                        sendDirectMessage(user, message);
                    }
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

    public static synchronized void updateRegPlayerMsg() {
        List<String> messageList = regPlayerMsg();
        Iterator<String> messageListIt = messageList.iterator();

        TextChannel playerListChan = njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL);
        MessageHistory msgHist = playerListChan.getHistoryAfter(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL, 100).complete();
        List<Message> discordPlayerListMessages = msgHist.getRetrievedHistory();

        int numMsgExist = discordPlayerListMessages.size();
        int numMsgToSend = messageList.size();
        int msgToSendMinusExist = numMsgToSend - numMsgExist;

        if (msgToSendMinusExist >= 0) {
            //edit existing messages
            for (int i = numMsgExist - 1; i >= 0; i--) {
                String msgId = discordPlayerListMessages.get(i).getId();
                njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).editMessageById(msgId, messageListIt.next()).queue();
            }
            //send new messages
            for (int i = 0; i < msgToSendMinusExist; i++) {
                njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).sendMessage(messageListIt.next()).queue();
            }
        } else {
            //edit existing messages
            for (int i = numMsgExist - 1; i >= numMsgExist - numMsgToSend; i--) {
                String msgId = discordPlayerListMessages.get(i).getId();
                njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).editMessageById(msgId, messageListIt.next()).queue();
            }
            //delete remaining messages
            for (int i = (numMsgExist - numMsgToSend) - 1; i >= 0; i--) {
                String msgId = discordPlayerListMessages.get(i).getId();
                njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).deleteMessageById(msgId).queue();
            }
        }
    }

    private static List<String> parseTournLinks(MessageReceivedEvent event) {
        List<String> tournLinks = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream("tourn_links.csv");
            Reader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);

            List<String> excelRow;
            while ((excelRow = CSVHelper.parseLine(reader)) != null) {
                tournLinks.add(excelRow.get(0));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            event.getChannel().sendMessage(BotMsgs.tournLinksNotFound).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tournLinks;
    }

    public static void sendTournInvites(MessageReceivedEvent event) {
        Connection conn = null;
        PreparedStatement prepSt = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;
            ResultSet rs;

            List<String> tournLinks = parseTournLinks(event);

            sql = "SELECT player_id FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " ORDER BY order_reg ASC;";
            prepSt = conn.prepareStatement(sql);
            rs = prepSt.executeQuery();
            List<Integer> regPlayerIds = new ArrayList<>();
            while (rs.next()) {
                regPlayerIds.add(rs.getInt("player_id"));
            }

            if (regPlayerIds.size() > tournLinks.size()) {
                //not enough tournament invite links
                event.getChannel().sendMessage(BotMsgs.notEnoughTournLinks(tournLinks.size(), regPlayerIds.size())).queue();
                return;
            }

            Iterator<Integer> regPlayersIdIt = regPlayerIds.iterator();
            Iterator<String> tournLinksIt = tournLinks.iterator();
            int playerCount = 0;
            while (regPlayersIdIt.hasNext() && tournLinksIt.hasNext() && playerCount < 64) {
                int playerId = regPlayersIdIt.next();
                String discordId = PlayerLookup.getDiscordId(playerId);
                User user = njal.getMemberById(discordId).getUser();
                if (user == null) {
                    //user left server, skip this user
                    continue;
                }
                String tournLink = tournLinksIt.next();
                sendDirectMessage(user, BotMsgs.tournLinkDM(tournLink));

                sql = "UPDATE " + SQLTableNames.SQL_TOURN_PLAYERS + " SET invite_link = ? WHERE player_id = ?;";
                prepSt = conn.prepareStatement(sql);
                prepSt.setString(1, tournLink);
                prepSt.setInt(2, playerId);
                prepSt.executeUpdate();

                playerCount++;
            }
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

    private static List<String> regPlayerMsg() {
        //TODO fix this to use listMessageBuilder()
        Connection conn = null;
        PreparedStatement prepSt = null;
        List<String> fullMessageArray = new ArrayList<>();

        try {
            conn = MyDBConnection.getConnection();
            ResultSet rs_playerInfo = null;
            ResultSet rs_tournPlayers;
            String sql_playerInfo;
            String sql_tournPlayers;

            List<String> orderRegCol = new ArrayList<>();
            List<String> discordNameCol = new ArrayList<>();
            List<String> discrimCol = new ArrayList<>();
            List<String> steamProfCol = new ArrayList<>();

            //labels
            orderRegCol.add("#");
            discordNameCol.add("Name");
            discrimCol.add("Discriminator");
            steamProfCol.add("`Steam Profile`");

            sql_tournPlayers = "SELECT player_id, order_reg FROM " + SQLTableNames.SQL_TOURN_PLAYERS + " ORDER BY order_reg ASC;";
            prepSt = conn.prepareStatement(sql_tournPlayers);
            rs_tournPlayers = prepSt.executeQuery();

            while (rs_tournPlayers.next()) {
                String orderReg = Integer.toString(rs_tournPlayers.getInt("order_reg"));
                int playerId = rs_tournPlayers.getInt("player_id");

                sql_playerInfo = "SELECT player_id, discrim, steam_id, discord_name FROM " + SQLTableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
                prepSt = conn.prepareStatement(sql_playerInfo);
                prepSt.setInt(1, playerId);
                rs_playerInfo = prepSt.executeQuery();
                rs_playerInfo.next();

                String discordName = rs_playerInfo.getString("discord_name");
                String discrim = Integer.toString(rs_playerInfo.getInt("discrim"));
                String steamProf = "http://steamcommunity.com/profiles/" + rs_playerInfo.getString("steam_id");

                orderRegCol.add(orderReg);
                discordNameCol.add(discordName);
                discrimCol.add(discrim);
                steamProfCol.add(steamProf);
            }

            List<List<String>> columnsList = new ArrayList<>();
            columnsList.add(orderRegCol);
            columnsList.add(discordNameCol);
            columnsList.add(discrimCol);

            //make each entry the same size per column
            ListIterator<List<String>> columnsListIt = columnsList.listIterator();
            while (columnsListIt.hasNext()) {
                List<String> column = columnsListIt.next();
                columnsListIt.set(evenStrLength(column));
            }
            //not necessary to adjust size of Steam Profile column
            columnsList.add(steamProfCol);

            //make full message
            String message = "```Registered Players```";
            int numRows = columnsList.get(0).size();
            int rowIndex = 0;
            String potentialMsg = "`";
            while (rowIndex < numRows) {
                //for each row, iterate through columns and add entries to potentialMsg
                columnsListIt = columnsList.listIterator();
                while (columnsListIt.hasNext()) {
                    List<String> column = columnsListIt.next();
                    if (column.equals(orderRegCol)) {
                        potentialMsg = potentialMsg.concat(column.get(rowIndex) + " ");
                    } else if (column.equals(discordNameCol)) {
                        potentialMsg = potentialMsg.concat(column.get(rowIndex) + " ");
                    } else if (column.equals(discrimCol)) {
                        potentialMsg = potentialMsg.concat(column.get(rowIndex) + "` ");
                    } else if (column.equals(steamProfCol)) {
                        if (rowIndex == 0) {
                            potentialMsg = potentialMsg.concat(column.get(rowIndex));
                        } else {
                            potentialMsg = potentialMsg.concat("<" + column.get(rowIndex) + ">");
                        }
                    }
                }

                //max char limit is 2000, so if concatening the potentialMsg to message will be less then 1996, go ahead and concat.
                //if not, add current message to array and then make a new message for the bot to send
                if ((message.length() + potentialMsg.length()) <= 1996) {
                    message = message.concat(potentialMsg);
                } else {
                    fullMessageArray.add(message);
                    message = potentialMsg;
                }

                //end of a row
                message = message.concat("\n");
                potentialMsg = "`";
                rowIndex++;
            }
            fullMessageArray.add(message);

            rs_tournPlayers.close();
            if (rs_playerInfo != null) {
                rs_playerInfo.close();
            }
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
        return fullMessageArray;
    }

    private static List<String> evenStrLength(List<String> entries) {
        int maxSize = 0;
        ListIterator<String> entriesIt = entries.listIterator();
        while (entriesIt.hasNext()) {
            String entry = entriesIt.next();
            if (entry.length() > maxSize) {
                maxSize = entry.length();
            }
        }

        entriesIt = entries.listIterator();
        while (entriesIt.hasNext()) {
            String entry = entriesIt.next();
            int neededSpaces = maxSize - entry.length();

            for (int i = 0; i < neededSpaces; i++) {
                entry = entry.concat(" ");
            }
            //add zero-width space so that the code block won't trim trailing spaces in discord
            entry = entry.concat("\u200B");
            entriesIt.set(entry);
        }
        return entries;
    }

    public static List<String> listMessageBuilder(List<List<String>> columnsList) {
        List<String> fullMessageList = new ArrayList<>();

        //make each entry the same size for each column
        ListIterator<List<String>> columnsListIt = columnsList.listIterator();
        while (columnsListIt.hasNext()) {
            List<String> column = columnsListIt.next();
            columnsListIt.set(SendMessage.evenStrLength(column));
        }

        //make full message
        String message = "``` ```";
        int numRows = columnsList.get(0).size();
        int rowIndex = 0;
        String potentialMsg = "`";
        while (rowIndex < numRows) {
            //for each row, iterate through columns and add entries to potentialMsg
            columnsListIt = columnsList.listIterator();
            while (columnsListIt.hasNext()) {
                List<String> column = columnsListIt.next();
                potentialMsg = potentialMsg.concat(column.get(rowIndex) + "` ");
            }
            potentialMsg = potentialMsg.trim();

            //max char limit is 2000, so if concating the potentialMsg to message will be less then 1996, go ahead and concat.
            //if not, add current message to array and then make a new message for the bot to send
            if ((message.length() + potentialMsg.length()) <= 1996) {
                message = message.concat(potentialMsg);
            } else {
                fullMessageList.add(message);
                message = potentialMsg;
            }

            //end of row
            message = message.concat("\n");
            potentialMsg = "`";
            rowIndex++;
        }
        fullMessageList.add(message);

        return fullMessageList;
    }
}
