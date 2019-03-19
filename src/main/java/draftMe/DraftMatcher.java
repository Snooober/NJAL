package draftMe;

import constants.BotMsgs;
import discordBot.DiscordBot;
import discordBot.SendMessage;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;

public class DraftMatcher {
    private static Map<Integer, String> pendMatchCodes = new HashMap<>();
    private static List<DraftPair> pendDraftPairs = new ArrayList<>();
    private static List<DraftRoom> draftRooms = new ArrayList<>();

    private DraftMatcher() {
    }

    public static void newDraftMatch(MessageReceivedEvent event) {
        User user = event.getAuthor();
        purgeUser(user);

        Integer matchCode = pendMatchCode(user);
        SendMessage.sendDirectMessage(user, BotMsgs.queryOpponentMatchCode(matchCode));
    }

    public static void matchUser(MessageReceivedEvent event, Integer matchCode) {
        User user = event.getAuthor();
        purgeUser(user);

        //check if match code exists
        if (pendMatchCodes.containsKey(matchCode)) {
            //check if !draftme initiator mistakenly sent match code
            if (pendMatchCodes.get(matchCode).equals(user.getId())) {
                event.getChannel().sendMessage(BotMsgs.wrongUserSentMatchCode(matchCode)).queue();
                return;
            }

            String playerId1 = pendMatchCodes.get(matchCode);
            String playerId2 = user.getId();
            DraftPair draftPair = new DraftPair(playerId1, playerId2);

            pendMatchCodes.remove(matchCode);
            pendDraftPairs.add(draftPair);

            event.getChannel().sendMessage(BotMsgs.matchPairMade).queue();
            User player1 = DiscordBot.njal.getMemberById(playerId1).getUser();
            SendMessage.sendDirectMessage(player1, BotMsgs.matchPairMade);

            matchPairs(matchCode);
        }

    }

    private static void matchPairs(Integer matchCode) {
        while (pendDraftPairs.size() > 1) {
            DraftPair pair1 = null;
            DraftPair pair2 = null;
            Iterator<DraftPair> draftPairIt = pendDraftPairs.iterator();
            if (draftPairIt.hasNext()) {
                pair1 = draftPairIt.next();
            }
            if (draftPairIt.hasNext()) {
                pair2 = draftPairIt.next();
            }

            if (pair1 != null && pair2 != null) {
                pendDraftPairs.remove(pair1);
                pendDraftPairs.remove(pair2);
                draftRooms.add(new DraftRoom(pair1, pair2, matchCode));
            }
        }
    }

    private static Integer pendMatchCode(User user) {
        Integer matchCode = ((Double) (Math.random() * 10000)).intValue();
        while (pendMatchCodes.containsKey(matchCode)) {
            matchCode = ((Double) (Math.random() * 10000)).intValue();
        }
        pendMatchCodes.put(matchCode, user.getId());

        return matchCode;
    }

    static void removeMatchCode(Integer matchCode) {
        pendMatchCodes.remove(matchCode);
    }

    public static void purgeUser(User user) {
        //close DraftRoom's
        List<DraftRoom> queueDeletion = new ArrayList<>();
        Iterator<DraftRoom> draftRoomsIt = draftRooms.iterator();
        while (draftRoomsIt.hasNext()) {
            DraftRoom draftRoom = draftRoomsIt.next();
            Iterator<String> playerIdIt = draftRoom.getPlayerIds().iterator();
            while (playerIdIt.hasNext()) {
                String playerId = playerIdIt.next();
                if (user.getId().equals(playerId)) {
                    draftRoom.deleteChannel();
                    queueDeletion.add(draftRoom);
                    break;
                }
            }
        }

        Iterator<DraftRoom> queueDelIt = queueDeletion.iterator();
        while (queueDelIt.hasNext()) {
            draftRooms.remove(queueDelIt.next());
        }
    }
}
