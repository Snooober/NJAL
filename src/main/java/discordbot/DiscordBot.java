package discordbot;

import constants.DiscordIds;
import constants.Properties;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class DiscordBot extends ListenerAdapter {
    public static final String NJAL_TITLE = ":small_red_triangle_down: **Nicely Jobbed! :thumbsup: Artifact League!** :small_red_triangle_down:";
    public static JDA rossBot;
    public static Role admin;
    public static Guild njal;

    private DiscordBot() {
    }

    private static void init() {
        admin = rossBot.getRoleById("490085860188880917");
        njal = rossBot.getGuildById(DiscordIds.NJAL_GUILD_ID);
    }

    public static void startRoss() {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Properties.getProps().token);
        builder.addEventListener(new MessageReceiver());
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
