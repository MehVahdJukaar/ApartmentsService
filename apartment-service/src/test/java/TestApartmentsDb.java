import apartments.Apartment;
import apartments.ApartmentsDAO;
import apartments.ApartmentsDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestApartmentsDb {

    private Connection conn;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize the database before each test
        ApartmentsDatabase.initialize();
        conn = ApartmentsDatabase.getConnection();
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
        Apartment apartment = new Apartment("Apartment 101", "123 Main St", 2, 5);
        ApartmentsDAO.addApartment(apartment);

        // Verify if the data was inserted correctly
        List<Apartment> apartments = ApartmentsDAO.getAllApartments();
        assertFalse(apartments.isEmpty(), "There should be at least one apartment in the list.");
        assertTrue(apartments.stream().anyMatch(a -> a.id().equals(apartment.id())), "Apartment with ID '1' should be present.");
    }

    @Test
    public void testGetAllApartments() throws Exception {
        // Ensure we can retrieve all apartments
        List<Apartment> apartments = ApartmentsDAO.getAllApartments();
        assertNotNull(apartments, "Apartments list should not be null.");
        assertTrue(!apartments.isEmpty(), "There should be at least one apartment in the list.");
    }

    @Test
    public void testDeleteAllApartments() throws Exception {
        // Delete all apartments from the database
        ApartmentsDAO.removeAllApartments();

        // Verify that the apartments list is empty
        List<Apartment> apartments = ApartmentsDAO.getAllApartments();
        assertTrue(apartments.isEmpty(), "Apartments list should be empty after deletion.");
    }
}
