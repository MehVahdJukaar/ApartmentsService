import bookings.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestBookingDb {

    private Connection conn;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize the database before each test
        BookingsDAO.initialize(false);
        conn = ((BookingsStateDAO) BookingsDAO.INSTANCE).database.getConnection();
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
    public void testAddBooking() throws Exception {
        // Prepare test data
        UUID apartmentId = UUID.randomUUID();
        Date fromDate = new Date(0);
        Date toDate = new Date(fromDate.getTime() + 1000 * 60 * 60 * 24);  // 1 day later
        String who = "John Doe";

        // Add a booking to the database
        Booking booking = new Booking(apartmentId, fromDate, toDate, who);
        boolean success = BookingsStateDAO.INSTANCE.addBooking(booking);

        // Check if the booking was successfully added
        assertTrue(success, "Booking should be added successfully.");
    }

    @Test
    public void testGetBookingById() throws Exception {
        // Prepare test data
        UUID apartmentId = UUID.randomUUID();
        Date fromDate = new Date(2000);
        Date toDate = new Date(fromDate.getTime() + 1000 * 60 * 60 * 24);  // 1 day later
        String who = "John Doe";

        // Add a booking to the database
        Booking booking = new Booking(apartmentId, fromDate, toDate, who);
        BookingsStateDAO.INSTANCE.addBooking(booking);

        // Retrieve the booking by ID
        Booking retrievedBooking = BookingsStateDAO.INSTANCE.getBookingById(booking.id());

        // Assert that the retrieved booking matches the original one
        assertNotNull(retrievedBooking, "Booking should be retrieved by ID.");
        assertEquals(booking.id(), retrievedBooking.id(), "Booking IDs should match.");
        assertEquals(booking.fromDate(), retrievedBooking.fromDate(), "From date should match.");
        assertEquals(booking.toDate(), retrievedBooking.toDate(), "To date should match.");
        assertEquals(booking.who(), retrievedBooking.who(), "Who should match.");
    }

    @Test
    public void testChangeBookingDates() throws Exception {
        // Prepare test data
        UUID apartmentId = UUID.randomUUID();
        Date fromDate = new Date(2000);
        Date toDate = new Date(fromDate.getTime() + 1000 * 60 * 60 * 24);  // 1 day later
        String who = "John Doe";

        // Add a booking to the database
        Booking booking = new Booking(apartmentId, fromDate, toDate, who);
        BookingsStateDAO.INSTANCE.addBooking(booking);

        // Change booking dates
        Date newFromDate = new Date(fromDate.getTime() + 1000 * 60 * 60 * 48);  // 2 days later
        Date newToDate = new Date(newFromDate.getTime() + 1000 * 60 * 60 * 24);  // 1 day after newFromDate
        boolean success = BookingsStateDAO.INSTANCE.changeBookingDates(booking.id(), newFromDate, newToDate);

        // Verify that the dates were updated successfully
        assertTrue(success, "Booking dates should be changed successfully.");

        // Retrieve the updated booking and check the dates
        Booking updatedBooking = BookingsStateDAO.INSTANCE.getBookingById(booking.id());
        assertNotNull(updatedBooking, "Updated booking should exist.");
        assertEquals(newFromDate, updatedBooking.fromDate(), "Updated from date should match.");
        assertEquals(newToDate, updatedBooking.toDate(), "Updated to date should match.");
    }

    @Test
    public void testRemoveBooking() throws Exception {
        // Prepare test data
        UUID apartmentId = UUID.randomUUID();
        Date fromDate = new Date(2000);
        Date toDate = new Date(fromDate.getTime() + 1000 * 60 * 60 * 24);  // 1 day later
        String who = "John Doe";

        // Add a booking to the database
        Booking booking = new Booking(apartmentId, fromDate, toDate, who);
        BookingsStateDAO.INSTANCE.addBooking(booking);

        // Remove the booking by ID
        boolean success = BookingsStateDAO.INSTANCE.cancelBooking(booking.id());

        // Verify that the booking was removed successfully
        assertTrue(success, "Booking should be removed successfully.");

        // Try to retrieve the removed booking
        Booking removedBooking = BookingsStateDAO.INSTANCE.getBookingById(booking.id());
        assertNull(removedBooking, "Removed booking should not be found.");
    }

    //@Test
    public void testRemoveAllBookings() throws Exception {
        // Prepare test data
        UUID apartmentId = UUID.randomUUID();
        Date fromDate = new Date(2000);
        Date toDate = new Date(fromDate.getTime() + 1000 * 60 * 60 * 24);  // 1 day later
        String who = "John Doe";

        // Add a booking to the database
        Booking booking = new Booking(apartmentId, fromDate, toDate, who);
        BookingsStateDAO.INSTANCE.addBooking(booking);

        // Remove all bookings
        BookingsStateDAO.INSTANCE.cancelAllBookings();

        // Verify that the bookings list is empty
        List<Booking> bookings = BookingsStateDAO.INSTANCE.listAllBookings();
        assertTrue(bookings.isEmpty(), "Bookings list should be empty after deletion.");
    }
}
