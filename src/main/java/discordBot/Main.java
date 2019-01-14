package discordBot;

import org.apache.log4j.BasicConfigurator;

/*
 * TODO add draft me system
 * TODO automate more:
 *      notify players before tourn starts (30 minutes/15 minutes?)
 *      handle unregistration automatically (including backing up data)
 *      handle reg lock/unlock
 * TODO review logging
 */

public class Main {
    public static void main(String[] args) {
        //SLF4J config
        BasicConfigurator.configure();
        DiscordBot.startRoss();
    }
}