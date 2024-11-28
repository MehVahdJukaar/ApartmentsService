package bookings;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingDAO {

    // Method to add a booking
    public static boolean addBooking(Booking booking) {
        String sql = "INSERT INTO bookings (id, apartment_id, from_date, to_date, who) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = BookingsDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, booking.id().toString()); // Set booking ID
            pstmt.setString(2, booking.apartmentID().toString()); // Set apartment ID
            pstmt.setDate(3, booking.fromDate()); // Set from date
            pstmt.setDate(4, booking.toDate()); // Set to date
            pstmt.setString(5, booking.who()); // Set user

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Method to retrieve a booking by its ID
    @Nullable
    public static Booking getBookingById(UUID bookingId) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        Booking booking = null;

        try (Connection conn = BookingsDatabase.getConnection();
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
    public static boolean cancelBooking(UUID bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = BookingsDatabase.getConnection();
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
    public static boolean changeBooking(UUID bookingId, Date newFromDate, Date newToDate) {
        String sql = "UPDATE bookings SET from_date = ?, to_date = ? WHERE id = ?";

        try (Connection conn = BookingsDatabase.getConnection();
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
    public static List<Booking> listAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings";

        try (Connection conn = BookingsDatabase.getConnection();
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
    private static Booking parseBooking(ResultSet rs) throws SQLException {
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
    public static boolean changeBookingDates(UUID bookingId, Date fromDate, Date toDate) {
        // First, check if the apartment is available for the new dates
        String sql = "UPDATE bookings SET from_date = ?, to_date = ? WHERE id = ?";

        try (Connection conn = BookingsDatabase.getConnection();
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

    public static void cancelAllBookings() {
        String sql = "DELETE FROM bookings";

        try (Connection conn = BookingsDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
