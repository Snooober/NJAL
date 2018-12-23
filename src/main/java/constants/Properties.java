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

            in.close();
        } catch (FileNotFoundException e) {
            //file does not exist
        } catch (IOException e) {
            //IO exception
        }
    }
}
