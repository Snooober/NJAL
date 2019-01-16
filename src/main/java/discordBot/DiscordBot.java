package discordBot;

import constants.DiscordIds;
import constants.Properties;
import draftMe.LeaveDraftMeEvents;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

import javax.security.auth.login.LoginException;

public class DiscordBot {
    public static JDA rossBot;
    public static Role admin;
    public static Guild njal;

    private DiscordBot() {
    }

    private static void init() {
        admin = rossBot.getRoleById(DiscordIds.RoleIds.ADMIN);
        njal = rossBot.getGuildById(DiscordIds.NJAL_GUILD_ID);
    }

    public static void startRoss() {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Properties.getProps().token);
        builder.addEventListener(new MessageReceiver());
        builder.addEventListener(new LeaveDraftMeEvents());
        builder.addEventListener(new LeaveServerListener());
        builder.addEventListener(new UpdateInfoListener());

        try {
            rossBot = builder.build();
            rossBot.awaitReady();
            init();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rossBot.getPresence().setGame(Game.playing("Hearthstone"));
    }
}
