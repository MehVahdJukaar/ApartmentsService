package bookings;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingDAO implements BookingDataAccess {

    // Method to add a booking
    public boolean addBooking(Booking booking) {
        // Check if the apartment exists before adding the booking
        if (!getApartment(booking.apartmentID())) {
            System.out.println("Apartment with ID " + booking.apartmentID() + " does not exist.");
            return false; // Return false if apartment doesn't exist
        }

        String sql = "INSERT INTO bookings (id, apartment_id, from_date, to_date, who) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = BookingDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, booking.id().toString()); // Set booking ID
            pstmt.setString(2, booking.apartmentID().toString()); // Set apartment ID
            pstmt.setDate(3, booking.fromDate()); // Set from date
            pstmt.setDate(4, booking.toDate()); // Set to date
            pstmt.setString(5, booking.who()); // Set user

            pstmt.executeUpdate();
            return true; // Return true if the booking is successfully added

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs
        }
    }

    // Method to retrieve a booking by its ID
    @Nullable
    public Booking getBookingById(UUID bookingId) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        Booking booking = null;

        try (Connection conn = BookingDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookingId.toString());  // Set the booking ID in the query

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                booking = parseBooking(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return booking;  // Return the booking (or null if not found)
    }

    // Method to cancel a booking by ID
    public boolean cancelBooking(UUID bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = BookingDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bookingId.toString());
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0; // Return true if the booking was canceled (deleted)

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to change an existing booking by ID
    public boolean changeBooking(UUID bookingId, Date newFromDate, Date newToDate) {
        String sql = "UPDATE bookings SET from_date = ?, to_date = ? WHERE id = ?";

        try (Connection conn = BookingDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, newFromDate); // Set the new from date
            pstmt.setDate(2, newToDate);   // Set the new to date
            pstmt.setString(3, bookingId.toString()); // Set booking ID

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if the booking was updated

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to list all bookings
    public List<Booking> listAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings";

        try (Connection conn = BookingDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Booking booking = parseBooking(rs);
                bookings.add(booking);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookings;
    }

    // Helper method to parse a result set into a Booking object
    @Nullable
    private Booking parseBooking(ResultSet rs) throws SQLException {
        try {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID apartmentId = UUID.fromString(rs.getString("apartment_id"));
            Date fromDate = rs.getDate("from_date");
            Date toDate = rs.getDate("to_date");
            String who = rs.getString("who");

            return new Booking(id, apartmentId, fromDate, toDate, who);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to change the booking dates if the apartment is available
    public boolean changeBookingDates(UUID bookingId, Date fromDate, Date toDate) {
        // First, check if the apartment is available for the new dates
        String sql = "UPDATE bookings SET from_date = ?, to_date = ? WHERE id = ?";

        try (Connection conn = BookingDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, fromDate);  // Set the new fromDate
            pstmt.setDate(2, toDate);    // Set the new toDate
            pstmt.setString(3, bookingId.toString());  // Set the booking ID to update

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;  // Return true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // Return false if the apartment is not available
    }

    public void cancelAllBookings() {
        String sql = "DELETE FROM bookings";

        try (Connection conn = BookingDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addApartment(UUID apartmentId, String name) {
        String insertApartmentQuery = "INSERT OR IGNORE INTO apartments (id, name) VALUES (?, ?);";
        try (Connection conn = BookingDatabase.getConnection();
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
        try (Connection conn = BookingDatabase.getConnection();
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
        try (Connection conn = BookingDatabase.getConnection();
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

        try (Connection conn = BookingDatabase.getConnection();
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

    public JSONElement dumpBookings() {
        return dump("bookings");
    }

    private JSONArray dump(String tableName) {
        String sql = "SELECT * FROM " + tableName; // Fetch all rows from the table
        JSONArray jsonArray = new JSONArray(); // Array to hold all rows

        try (Connection conn = BookingDatabase.getConnection();
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
