package search;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SearchDAO {

    // Add an apartment to the database
    public static void addApartment(UUID apartmentId, String name) {
        String insertApartmentQuery = "INSERT OR IGNORE INTO apartments (id, name) VALUES (?, ?);";
        try (Connection conn = SearchDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertApartmentQuery)) {
            stmt.setString(1, apartmentId.toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove an apartment from the database
    public static void removeApartment(UUID apartmentId) {
        String deleteApartmentQuery = "DELETE FROM apartments WHERE id = ?;";
        String deleteBookingsQuery = "DELETE FROM bookings WHERE apartment_id = ?;";
        try (Connection conn = SearchDatabase.getConnection();
             PreparedStatement deleteBookingsStmt = conn.prepareStatement(deleteBookingsQuery);
             PreparedStatement deleteApartmentStmt = conn.prepareStatement(deleteApartmentQuery)) {
            deleteBookingsStmt.setString(1, apartmentId.toString());
            deleteBookingsStmt.executeUpdate();

            deleteApartmentStmt.setString(1, apartmentId.toString());
            deleteApartmentStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllApartments() {
        String query = "SELECT name FROM apartments;";
        List<String> apartments = new ArrayList<>();
        try (Connection conn = SearchDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                apartments.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartments;
    }

    public static List<String> getAllBookings() {
        String query = "SELECT id FROM bookings;";
        List<String> bookings = new ArrayList<>();
        try (Connection conn = SearchDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                bookings.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // Add a booking for an apartment
    public static void addBooking(UUID bookingId, UUID apartmentId, Date startDate, Date endDate) {
        String insertBookingQuery = "INSERT INTO bookings (id, apartment_id, start_date, end_date) VALUES (?, ?, ?, ?);";
        try (Connection conn = SearchDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertBookingQuery)) {
            stmt.setString(1, bookingId.toString());
            stmt.setString(2, apartmentId.toString());
            stmt.setDate(3, startDate);
            stmt.setDate(4, endDate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove a booking from the database
    public static void removeBooking(UUID bookingId) {
        String deleteBookingQuery = "DELETE FROM bookings WHERE id = ?;";
        try (Connection conn = SearchDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteBookingQuery)) {
            stmt.setString(1, bookingId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get a list of available apartments for a given date range
    public static List<String> getAvailableApartments(String checkInDate, String checkOutDate) {
        String query = """
                    SELECT a.name
                    FROM apartments a
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM bookings b
                        WHERE b.apartment_id = a.id
                          AND b.start_date < ?  -- Booking ends after check-in
                          AND b.end_date > ?    -- Booking starts before check-out
                    );
                """;
        List<String> availableApartments = new ArrayList<>();
        try (Connection conn = SearchDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, checkOutDate);
            stmt.setString(2, checkInDate);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                availableApartments.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableApartments;
    }

    public static void clearAllData() {
        String deleteBookingsQuery = "DELETE FROM bookings;";
        String deleteApartmentsQuery = "DELETE FROM apartments;";
        try (Connection conn = SearchDatabase.getConnection();
             PreparedStatement deleteBookingsStmt = conn.prepareStatement(deleteBookingsQuery);
             PreparedStatement deleteApartmentsStmt = conn.prepareStatement(deleteApartmentsQuery)) {
            deleteBookingsStmt.executeUpdate();
            deleteApartmentsStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
