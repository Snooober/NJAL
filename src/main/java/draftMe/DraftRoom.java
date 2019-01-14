package draftMe;

import constants.BotMsgs;
import constants.DiscordIds;
import discordBot.DiscordBot;
import discordBot.SendMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

class DraftRoom {
    private List<String> playerIds;
    private DraftPair pair1;
    private DraftPair pair2;
    private String channelId;
    private Integer matchCode;

    DraftRoom(DraftPair pair1, DraftPair pair2, Integer matchCode) {
        this.pair1 = pair1;
        this.pair2 = pair2;
        this.matchCode = matchCode;

        playerIds = new ArrayList<>();
        playerIds.add(pair1.getPlayer1Id());
        playerIds.add(pair1.getPlayer2Id());
        playerIds.add(pair2.getPlayer1Id());
        playerIds.add(pair2.getPlayer2Id());

        //create text channel for DraftRoom
        makeTextChannel();
    }

    private void makeTextChannel() {
        Member pair1player1 = DiscordBot.njal.getMemberById(pair1.getPlayer1Id());
        Member pair1player2 = DiscordBot.njal.getMemberById(pair1.getPlayer2Id());
        Member pair2player1 = DiscordBot.njal.getMemberById(pair2.getPlayer1Id());
        Member pair2player2 = DiscordBot.njal.getMemberById(pair2.getPlayer2Id());
        String channelName = pair1player1.getEffectiveName() + "-draft-room";

        //channel permissions for each member
        EnumSet<Permission> allowP = EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL);

        //create channel
        Category draftAFriendCat = DiscordBot.njal.getCategoryById(DiscordIds.CategoryIds.DRAFT_ME_CAT_ID);
        Channel channel = draftAFriendCat.createTextChannel(channelName).setParent(draftAFriendCat).addPermissionOverride(pair1player1, allowP, null).addPermissionOverride(pair1player2, allowP, null).addPermissionOverride(pair2player1, allowP, null).addPermissionOverride(pair2player2, allowP, null).complete();
        this.channelId = channel.getId();

        //message channel
        DiscordBot.njal.getTextChannelById(channelId).sendMessage(BotMsgs.draftMeChannelInstructions(pair1player1.getEffectiveName(), pair1player2.getEffectiveName(), pair2player1.getEffectiveName(), pair2player2.getEffectiveName())).queue();

        //direct message players link to channel
        Iterator<String> playerIdIt = playerIds.iterator();
        while (playerIdIt.hasNext()) {
            User user = DiscordBot.njal.getMemberById(playerIdIt.next()).getUser();
            SendMessage.sendDirectMessage(user, BotMsgs.draftMeRoomFoundDM(channelId));
        }
    }

    void deleteChannel() {
        Channel channel = DiscordBot.njal.getTextChannelById(channelId);
        channel.delete().queue();

        //direct message channel members
        Iterator<String> playerIdIt = playerIds.iterator();
        while (playerIdIt.hasNext()) {
            User user = DiscordBot.njal.getMemberById(playerIdIt.next()).getUser();
            SendMessage.sendDirectMessage(user, BotMsgs.draftMeRoomClosedDM);
        }

        //delete associated match code
        DraftMatcher.removeMatchCode(matchCode);
    }

    List<String> getPlayerIds() {
        return playerIds;
    }
}
