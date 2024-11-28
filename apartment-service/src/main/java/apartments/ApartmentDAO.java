package apartments;

import apartments.Apartment;
import apartments.ApartmentsDatabase;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApartmentDAO {

    // Method to add an apartment to the database
    public static boolean addApartment(Apartment apartment) {
        String sql = "INSERT INTO apartments (id, name, address, noiselevel, floor) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ApartmentsDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameters for the query
            pstmt.setString(1, apartment.id().toString()); // Set ID (UUID, generated earlier)
            pstmt.setString(2, apartment.name()); // Set name
            pstmt.setString(3, apartment.address()); // Set address
            pstmt.setInt(4, apartment.noiseLevel()); // Set noise level
            pstmt.setInt(5, apartment.floor()); // Set floor

            // Execute the update (insert)
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to retrieve an apartment by its ID
    @Nullable
    public static Apartment getApartmentById(UUID id) {
        String sql = "SELECT * FROM apartments WHERE id = ?";
        Apartment apartment = null;

        try (Connection conn = ApartmentsDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the ID in the prepared statement
            pstmt.setString(1, id.toString());

            // Execute the query
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Retrieve apartment data from the result set
                apartment = parseApartment(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apartment;  // Return the apartment (or null if not found)
    }


    // Method to retrieve all apartments from the database
    public static List<Apartment> getAllApartments() {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT * FROM apartments";

        try (Connection conn = ApartmentsDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Apartment apartment = parseApartment(rs);
                if (apartment != null) apartments.add(apartment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return apartments;
    }


    // Method to remove an apartment by ID
    public static boolean removeApartmentById(UUID apartmentId) {
        String sql = "DELETE FROM apartments WHERE id = ?";

        try (Connection conn = ApartmentsDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, apartmentId.toString());
            int rowsAffected = pstmt.executeUpdate();

            // If rowsAffected > 0, that means an apartment was deleted
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void removeAllApartments() {
        String sql = "DELETE FROM apartments";

        try (Connection conn = ApartmentsDatabase.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Nullable
    private static Apartment parseApartment(ResultSet rs) throws SQLException {
        try {
            UUID id = UUID.fromString(rs.getString("id"));
            String name = rs.getString("name");
            String address = rs.getString("address");
            int noiseLevel = rs.getInt("noiseLevel");
            int floor = rs.getInt("floor");

            // Create an Apartment object and add it to the list
            return new Apartment(id, name, address, noiseLevel, floor);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}