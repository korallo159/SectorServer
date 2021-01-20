package koral.sectorserver.database.statements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static koral.sectorserver.database.DatabaseConnection.hikari;
public class Table {

    public static void createTable() {
        Connection connection = null;
        PreparedStatement statement = null;
        String create = "CREATE TABLE IF NOT EXISTS Players(NICK VARCHAR(16), UUID VARCHAR(36), playerdata MEDIUMTEXT, PRIMARY KEY (NICK))";

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement(create);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }
}
