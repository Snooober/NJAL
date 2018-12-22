package discordbot;

import constants.DiscordIds;
import constants.SQL_TableNames;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import sqlhandlers.MyDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SendMessage {
    public static void updateRegPlayerMsg() {
        List<String> messageList = regPlayerMsg();
        Iterator<String> messageListIt = messageList.iterator();

        TextChannel playerListChan = DiscordBot.njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL);
        MessageHistory msgHist = playerListChan.getHistoryAfter(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL, 100).complete();
        List<Message> discordPlayerListMessages = msgHist.getRetrievedHistory();

        int numMsgExist = discordPlayerListMessages.size();
        int numMsgToSend = messageList.size();
        int msgToSendMinusExist = numMsgToSend - numMsgExist;

        if (msgToSendMinusExist >= 0) {
            //edit existing messages
            for (int i = numMsgExist - 1; i >= 0; i--) {
                String msgId = discordPlayerListMessages.get(i).getId();
                DiscordBot.njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).editMessageById(msgId, messageListIt.next()).queue();
            }
            //send new messages
            for (int i = 0; i < msgToSendMinusExist; i++) {
                DiscordBot.njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).sendMessage(messageListIt.next()).queue();
            }
        } else {
            //edit existing messages
            for (int i = numMsgExist - 1; i >= numMsgExist - numMsgToSend; i--) {
                String msgId = discordPlayerListMessages.get(i).getId();
                DiscordBot.njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).editMessageById(msgId, messageListIt.next()).queue();
            }
            //delete remaining messages
            for (int i = (numMsgExist - numMsgToSend) - 1; i >= 0; i--) {
                String msgId = discordPlayerListMessages.get(i).getId();
                DiscordBot.njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL).deleteMessageById(msgId).queue();
            }
        }
    }

    private static List<String> regPlayerMsg() {
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

            sql_tournPlayers = "SELECT player_id, order_reg FROM " + SQL_TableNames.SQL_TOURN_PLAYERS + " ORDER BY order_reg ASC;";
            prepSt = conn.prepareStatement(sql_tournPlayers);
            rs_tournPlayers = prepSt.executeQuery();

            while (rs_tournPlayers.next()) {
                String orderReg = Integer.toString(rs_tournPlayers.getInt("order_reg"));
                int playerId = rs_tournPlayers.getInt("player_id");

                sql_playerInfo = "SELECT player_id, discrim, steam_id, discord_name FROM " + SQL_TableNames.SQL_PLAYER_INFO + " WHERE player_id = ?;";
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
            while (rowIndex < numRows) {
                String potentialMsg = "`";

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
}
