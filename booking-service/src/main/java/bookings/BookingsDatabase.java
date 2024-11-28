package bookings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BookingsDatabase {
    private static final String DB_PATH = System.getenv("BOOKING_DB_PATH") != null ?
            System.getenv("BOOKING_DB_PATH") : "bookings.db";

    private static Connection connection;

    // Connect to SQLite database
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) { // Ensure the connection is open
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        }
        return connection;
    }

    // Initialize the database (create tables if they don't exist)
    public static void initialize() {
        try (Connection conn = getConnection(); // This line ensures the connection is open
             Statement stmt = conn.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS bookings (" +
                    "id TEXT PRIMARY KEY," +
                    "apartment_id TEXT NOT NULL," +
                    "from_date DATE NOT NULL," +
                    "to_date DATE NOT NULL," +
                    "who TEXT NOT NULL" +
                    ");";
            stmt.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

