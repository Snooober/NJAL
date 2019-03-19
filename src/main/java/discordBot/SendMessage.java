package discordBot;

import constants.BotMsgs;
import constants.DiscordIds;
import constants.SQLTableNames;
import helpers.CSVHelper;
import helpers.MyDBConnection;
import helpers.PlayerLookup;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tournManager.*;

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

import static discordBot.DiscordBot.njal;

public class SendMessage {

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

    private static synchronized void updateDiscordChannelMsgs(List<String> messageList, TextChannel channel) {
        List<Message> channelMsgs = channel.getHistory().getRetrievedHistory();

        int numMsgExist = channelMsgs.size();
        int numMsgToSend = messageList.size();
        int msgToSendMinusExist = numMsgToSend - numMsgExist;

        Iterator<String> messageListIt = messageList.iterator();
        if (msgToSendMinusExist >= 0) {
            //edit existing messages
            for (int i = numMsgExist - 1; i >= 0; i--) {
                String msgId = channelMsgs.get(i).getId();
                channel.editMessageById(msgId, messageListIt.next()).queue();
            }
            //send new messages
            for (int i = 0; i < msgToSendMinusExist; i++) {
                channel.sendMessage(messageListIt.next()).queue();
            }
        } else {
            //edit existing messages
            for (int i = numMsgExist - 1; i >= numMsgExist - numMsgToSend; i--) {
                String msgId = channelMsgs.get(i).getId();
                channel.editMessageById(msgId, messageListIt.next()).queue();
            }
            //delete remaining messages
            for (int i = (numMsgExist - numMsgToSend) - 1; i >= 0; i--) {
                String msgId = channelMsgs.get(i).getId();
                channel.deleteMessageById(msgId).queue();
            }
        }
    }

    public static void updateRegPlayerMsg() {
        updateDiscordChannelMsgs(regPlayerMsg(), njal.getTextChannelById(DiscordIds.ChannelIds.PLAYER_LIST_CHANNEL));
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
        String potentialMsg = "";
        while (rowIndex < numRows) {
            //for each row, iterate through columns and add entries to potentialMsg
            columnsListIt = columnsList.listIterator();
            while (columnsListIt.hasNext()) {
                List<String> column = columnsListIt.next();
                potentialMsg = potentialMsg.concat("`" + column.get(rowIndex) + "` ");
            }
            potentialMsg = potentialMsg.trim();

            //max char limit is 2000, so if concatenating the potentialMsg to message will be less then 1996, go ahead and concat.
            //if not, add current message to array and then make a new message for the bot to send
            if ((message.length() + potentialMsg.length()) <= 1996) {
                message = message.concat(potentialMsg);
            } else {
                fullMessageList.add(message);
                message = potentialMsg;
            }

            //end of row
            message = message.concat("\n");
            potentialMsg = "";
            rowIndex++;
        }
        fullMessageList.add(message);

        return fullMessageList;
    }

    public static void updateOverallStatsMsgs() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = MyDBConnection.getConnection();
            String sql;

            sql = "SELECT player_id, discord_name, wins, games_played, byes, tourn_wins FROM ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, SQLTableNames.SQL_PLAYER_INFO);
            resultSet = stmt.executeQuery();

            List<PlayerRank> playerRankList = new ArrayList<>();
            while (resultSet.next()) {
                PlayerRank playerRank = new PlayerRank(resultSet.getInt("player_id"), resultSet.getString("discord_name"), resultSet.getInt("wins"), resultSet.getInt("games_played"), resultSet.getInt("byes"), resultSet.getInt("tourn_wins"));
                playerRankList.add(playerRank);
            }

            //sort by rank (tournament wins, then by wins-games)
            playerRankList.sort(new TournWinsComparator());

            //make columns and columnsArray
            List<String> rankColumn = new ArrayList<>();
            List<String> nameColumn = new ArrayList<>();
            List<String> winsColumn = new ArrayList<>();
            List<String> lossColumn = new ArrayList<>();
            List<String> tournWinsColumn = new ArrayList<>();
            List<List<String>> columnsArray = new ArrayList<>();

            //add labels for first row
            rankColumn.add("Rank");
            nameColumn.add("Name");
            winsColumn.add("Wins");
            lossColumn.add("Losses");
            tournWinsColumn.add("Tournament Wins");

            //add each row
            Iterator<PlayerRank> playerIterator = playerRankList.iterator();
            Integer rank = 1;
            while (playerIterator.hasNext()) {
                PlayerRank player = playerIterator.next();
                String discordName = player.getDiscordName();
                Integer wins = player.getWins();
                Integer losses = (player.getGamesPlayed() - player.getWins());
                Integer tournWins = player.getTournWins();

                rankColumn.add(rank.toString());
                nameColumn.add(discordName);
                winsColumn.add(wins.toString());
                lossColumn.add(losses.toString());
                tournWinsColumn.add(tournWins.toString());

                rank++;
            }

            //add columns to columns array
            columnsArray.add(rankColumn);
            columnsArray.add(nameColumn);
            columnsArray.add(winsColumn);
            columnsArray.add(lossColumn);
            columnsArray.add(tournWinsColumn);

            //build entries into array for discord bot to send
            List<String> messageArray = listMessageBuilder(columnsArray);

            //update Overall Standings channel message(s)
            updateDiscordChannelMsgs(messageArray, njal.getTextChannelById(DiscordIds.ChannelIds.OVERALL_STANDINGS_CHANNEL));

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

    public static void sendStandings(Tournament tournament) {
        List<Player> playerList = tournament.getPlayerList();
        int currentRoundId = tournament.getCurrentRound().getRoundId();
        playerList.sort(new PlayerStandingsComparator());
        Player winner = null;
        if (tournament.onFinalRound()) {
            winner = playerList.get(0);
        }

        List<String> rankCol = new ArrayList<>();
        List<String> nameCol = new ArrayList<>();
        List<String> winsCol = new ArrayList<>();
        List<String> lossCol = new ArrayList<>();
        List<String> byesCol = new ArrayList<>();
        List<String> opponentCol = new ArrayList<>();
        List<List<String>> colList = new ArrayList<>();

        //Add labels
        rankCol.add("Rank");
        nameCol.add("Name");
        winsCol.add("Wins");
        lossCol.add("Losses");
        byesCol.add("Byes");
        opponentCol.add("Next opponent");

        int rank = 1;
        for (Player player :
                playerList) {
            int playerId = player.getPlayerId();
            String discordId = PlayerLookup.getDiscordId(playerId);
            String discordName = PlayerLookup.getDiscordName(discordId);
            int wins = player.getNumWins();
            int gamesPlayed = player.getGamesPlayed();
            int byes = player.getNumByes();
            int losses = gamesPlayed - wins;
            Player opponent = player.getCurrentOpponent();
            String opponentName;
            if (opponent == null) {
                opponentName = "Bye";
            } else {
                opponentName = PlayerLookup.getDiscordName(PlayerLookup.getDiscordId(opponent.getPlayerId()));
            }

            //Add entries for each player (in order of rank)
            rankCol.add(String.valueOf(rank));
            nameCol.add(discordName);
            winsCol.add(String.valueOf(wins));
            lossCol.add(String.valueOf(losses));
            byesCol.add(String.valueOf(byes));
            opponentCol.add(opponentName);

            //Send DM's
            User user = njal.getMemberById(discordId).getUser();
            if (currentRoundId == 0) {
                SendMessage.sendDirectMessage(user, BotMsgs.StandingsMsgs.tournStartDM(opponentName));
            } else if (tournament.onFinalRound() && winner != null) {
                if (playerId == winner.getPlayerId()) {
                    SendMessage.sendDirectMessage(user, BotMsgs.StandingsMsgs.finalRoundWinnerDM);
                } else {
                    SendMessage.sendDirectMessage(user, BotMsgs.StandingsMsgs.finalRoundDM);
                }
            } else {
                SendMessage.sendDirectMessage(user, BotMsgs.StandingsMsgs.roundCompleteDM(currentRoundId, opponentName));
            }

            rank++;
        }

        //Add columns to colList
        if (currentRoundId != 0) {
            colList.add(rankCol);
        }
        colList.add(nameCol);
        colList.add(winsCol);
        colList.add(lossCol);
        colList.add(byesCol);
        if (!tournament.onFinalRound()) {
            colList.add(opponentCol);
        }

        //Send standings
        List<String> fullMsgArray = listMessageBuilder(colList);
        for (String message :
                fullMsgArray) {
            njal.getTextChannelById(DiscordIds.ChannelIds.STANDINGS_REPORT_CHANNEL).sendMessage(message).complete();
        }

        //Send message after standings
        if (currentRoundId == 0) {
            njal.getTextChannelById(DiscordIds.ChannelIds.STANDINGS_REPORT_CHANNEL).sendMessage(BotMsgs.StandingsMsgs.tournStartStandingsMsg).queue();
        } else if (tournament.onFinalRound() && winner != null) {
            njal.getTextChannelById(DiscordIds.ChannelIds.STANDINGS_REPORT_CHANNEL).sendMessage(BotMsgs.StandingsMsgs.finalRoundStandingsMsg(PlayerLookup.getDiscordName(PlayerLookup.getDiscordId(winner.getPlayerId())))).queue();
        } else {
            njal.getTextChannelById(DiscordIds.ChannelIds.STANDINGS_REPORT_CHANNEL).sendMessage(BotMsgs.StandingsMsgs.roundCompleteStandingsMsg(currentRoundId)).queue();
        }
    }
}