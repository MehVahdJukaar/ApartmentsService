package bookings;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Would be better as an interface... need it for static
public abstract sealed class BookingsDAO permits BookingsStateDAO, BookingsEventSourcingDAO {

    public static BookingsDAO INSTANCE;

    public static void initialize(boolean usesEventSourcing) {
        INSTANCE = usesEventSourcing ? new BookingsEventSourcingDAO() : new BookingsStateDAO();
    }

    protected abstract Connection getConnection() throws SQLException;

    // Modify State

    public abstract boolean addBooking(Booking booking);

    public abstract boolean cancelBooking(UUID bookingId);

    public abstract boolean changeBookingDates(UUID bookingId, Date fromDate, Date toDate);

    public abstract void cancelAllBookings();


    // Getters

    @Nullable
    public abstract Booking getBookingById(UUID bookingId);

    public abstract List<Booking> listAllBookings();

    // Debug

    public abstract JSONElement dumpBookings();



    public void addApartment(UUID apartmentId, String name) {
        String insertApartmentQuery = "INSERT OR IGNORE INTO apartments (id, name) VALUES (?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertApartmentQuery)) {
            stmt.setString(1, apartmentId.toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean removeApartment(UUID apartmentId) {
        String deleteApartmentQuery = "DELETE FROM apartments WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteApartmentQuery)) {
            stmt.setString(1, apartmentId.toString()); // Convert UUID to string
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Return true if the apartment was removed
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if the apartment was not removed
    }

    // Get a list of all apartments from the apartments table
    public List<UUID> listApartments() {
        String selectApartmentsQuery = "SELECT id FROM apartments;";
        List<UUID> apartments = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectApartmentsQuery)) {
            while (rs.next()) {
                String apartmentId = rs.getString("id");
                apartments.add(UUID.fromString(apartmentId)); // Convert string back to UUID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }

    public boolean getApartment(UUID apartmentId) {
        String sql = "SELECT COUNT(*) FROM apartments WHERE id = ?"; // Assuming 'apartments' table exists

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, apartmentId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if the apartment exists
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Returns false if no apartment is found
    }

    public JSONElement dumpApartments() {
        return dump("apartments");
    }

    protected JSONArray dump(String tableName) {
        String sql = "SELECT * FROM " + tableName; // Fetch all rows from the table
        JSONArray jsonArray = new JSONArray(); // Array to hold all rows

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {


            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                JSONObject jsonObject = new JSONObject(); // Object for each row

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i); // Get column name
                    Object value = rs.getObject(i); // Get column value
                    jsonObject.put(columnName, value);
                }

                jsonArray.put(jsonObject); // Add row to array
            }

            return jsonArray; // Return JSON array as string

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jsonArray; // Return empty JSON array if an error occurs
    }
}
