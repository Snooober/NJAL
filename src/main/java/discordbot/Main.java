package discordbot;

import org.apache.log4j.BasicConfigurator;

/*
 * TODO unregister player when they leave server
 * TODO add draft me system
 * TODO automate more:
 *      notify players before tourn starts (30 minutes/15 minutes?)
 *      handle unregistration automatically (including backing up data)
 *      handle reg lock/unlock
 * TODO add command to force updating player_info
 * TODO review logging
 */

public class Main {
    public static void main(String[] args) {
        //SLF4J config
        BasicConfigurator.configure();
        DiscordBot.startRoss();
    }
}