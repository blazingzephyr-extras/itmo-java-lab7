package se.ifmo.blazingzephyr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    
    public DatabaseManager() {

    }

    public void connect() {

        String url = "jdbc:postgresql://localhost/test";
        Properties props = new Properties();
        props.setProperty("user", "server");
        props.setProperty("password", "secret");
        props.setProperty("ssl", "true");

        try {
            Connection connection = DriverManager.getConnection(url, props);
        }
        catch (SQLException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
