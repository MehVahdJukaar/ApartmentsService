package org.apartments;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {
    private static Connection connection;

    // Connect to SQLite database
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) { // Ensure the connection is open
            connection = DriverManager.getConnection("jdbc:sqlite:apartments.db");
        }
        return connection;
    }

    // Initialize the database (create tables if they don't exist)
    public static void initialize() {
        try (Connection conn = getConnection(); // This line ensures the connection is open
             Statement stmt = conn.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS apartments (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "address TEXT, " +
                    "noiseLevel INTEGER, " +
                    "floor INTEGER)";
            stmt.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

