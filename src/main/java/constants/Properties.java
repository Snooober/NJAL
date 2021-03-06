package constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Properties {
    private static Properties instance = new Properties();
    public String dbUrl;
    public String user;
    public String pass;
    public String token;

    String njalGuildId;
    String standingsReportChannel;
    String rossLogChannel;
    String draftMeChannel;
    String registerChannel;
    String playerListChannel;
    String overallStandingsChannel;
    String adminCommandsChannel;
    String draftMeCatId;
    String adminRole;

    private Properties() {
        setProperties();
    }

    public static Properties getProps() {
        return instance;
    }

    private void setProperties() {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties");
            java.util.Properties props = new java.util.Properties();
            props.load(in);

            dbUrl = props.getProperty("DB_URL");
            user = props.getProperty("user");
            pass = props.getProperty("pass");
            token = props.getProperty("token");

            if (pass.length() == 0) {
                pass = null;
            }

            njalGuildId = props.getProperty("guildId");
            standingsReportChannel = props.getProperty("standingsReportChannel");
            rossLogChannel = props.getProperty("logChannel");
            draftMeChannel = props.getProperty("draftMeChannel");
            registerChannel = props.getProperty("registerChannel");
            playerListChannel = props.getProperty("playerListChannel");
            overallStandingsChannel = props.getProperty("overallStandingsChannel");
            adminCommandsChannel = props.getProperty("adminCommandsChannel");
            draftMeCatId = props.getProperty("draftMeCatId");
            adminRole = props.getProperty("adminRole");

            in.close();
        } catch (FileNotFoundException e) {
            //file does not exist
        } catch (IOException e) {
            //IO exception
        }
    }
}
