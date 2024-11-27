
import org.apartments.Apartment;
import org.apartments.ApartmentDAO;
import org.apartments.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDb {

    private Connection conn;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize the database before each test
        Database.initialize();
        conn = Database.getConnection();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();  // Close connection after each test to prevent issues
        }
    }

    @Test
    public void testDatabaseConnection() throws Exception {
        // Ensure connection is not null
        assertNotNull(conn, "Connection should not be null.");
    }

    @Test
    public void testTableCreation() throws Exception {
        // Create a statement to query the database
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='apartments';";
        var stmt = conn.createStatement();
        var rs = stmt.executeQuery(query);
        assertTrue(rs.next(), "The 'apartments' table should exist.");
    }

    @Test
    public void testInsertData() throws Exception {
        // Insert a test record into the apartments table
        Apartment apartment = new Apartment( "Apartment 101", "123 Main St", 2, 5);
        ApartmentDAO.addApartment(apartment);

        // Verify if the data was inserted correctly
        List<Apartment> apartments = ApartmentDAO.getAllApartments();
        assertFalse(apartments.isEmpty(), "There should be at least one apartment in the list.");
        assertTrue(apartments.stream().anyMatch(a -> a.id().equals(apartment.id())), "Apartment with ID '1' should be present.");
    }

    @Test
    public void testGetAllApartments() throws Exception {
        // Ensure we can retrieve all apartments
        List<Apartment> apartments = ApartmentDAO.getAllApartments();
        assertNotNull(apartments, "Apartments list should not be null.");
        assertTrue(!apartments.isEmpty(), "There should be at least one apartment in the list.");
    }
}
