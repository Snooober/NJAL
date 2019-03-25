package discordBot;

import org.apache.log4j.BasicConfigurator;

public class Main {
    public static void main(String[] args) {
        //SLF4J config
        BasicConfigurator.configure();
        DiscordBot.startRoss();
    }
}