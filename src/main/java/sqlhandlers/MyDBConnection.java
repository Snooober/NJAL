package sqlhandlers;

import constants.Properties;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDBConnection {
    private MyDBConnection() {
    }

    public static java.sql.Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Properties.getProps().dbUrl, Properties.getProps().user, Properties.getProps().pass);
    }
}
