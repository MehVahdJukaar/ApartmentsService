package bookings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BookingsEventSourcingDatabase {

    private static final String DB_PATH = System.getenv("BOOKING_DB_PATH") != null ?
            System.getenv("BOOKING_ES_DB_PATH") : "bookings_es.db";

    // Connect to SQLite database
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }

    // Initialize the database (create tables if they don't exist)

    protected BookingsEventSourcingDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Event log stores all state changes
            String createEventLogTable = "CREATE TABLE IF NOT EXISTS event_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event_type TEXT NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +

                    "booking_id TEXT," +
                    "apartment_id TEXT," +
                    "from_date DATE," +
                    "to_date DATE," +
                    "who TEXT" +
                    ");";

            // Create the apartments table
            String createApartmentsTableQuery = "CREATE TABLE IF NOT EXISTS apartments (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL" +  // Add more fields as needed
                    ");";


            stmt.execute(createEventLogTable);
            stmt.execute(createApartmentsTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
