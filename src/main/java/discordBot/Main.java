package discordBot;

import org.apache.log4j.BasicConfigurator;

/*
 * TODO notify players before tourn starts (30 minutes/15 minutes?)
 * TODO review logging
 */

public class Main {
    public static void main(String[] args) {
        //SLF4J config
        BasicConfigurator.configure();
        DiscordBot.startRoss();
    }
}