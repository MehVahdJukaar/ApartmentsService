package bookings;

import kong.unirest.json.JSONElement;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BookingsEventSourcingDAO extends BookingsDAO {

    //visible for testing
    public final BookingsEventSourcingDatabase database = new BookingsEventSourcingDatabase();

    @Override
    public Connection getConnection() throws SQLException {
        return database.getConnection();
    }

    // Method to build the state of bookings from the event log at this point in time
    public Map<UUID, Booking> buildState() {
        Map<UUID, Booking> bookings = new HashMap<>();

        // SQL query to retrieve all events for all bookings ordered by timestamp
        String query = "SELECT event_type, booking_id, timestamp, apartment_id, from_date, to_date, who FROM event_log";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                EventType eventType = EventType.fromString(rs.getString("event_type"));
                // Rebuild the state for each booking

                switch (eventType) {
                    case BOOKING_ADDED -> {
                        Booking booking = parseBooking(rs);
                        bookings.put(booking.id(), booking);
                    }
                    case BOOKING_CHANGED -> {
                        Date fromDate = rs.getDate("from_date");
                        Date toDate = rs.getDate("to_date");
                        Booking existingBooking = bookings.get(UUID.fromString(rs.getString("booking_id")));
                        if (existingBooking == null) {
                            throw new AssertionError("Booking not found for change event");
                        } else {
                            Booking booking = new Booking(existingBooking.id(), existingBooking.apartmentID(), fromDate, toDate, existingBooking.who());
                            bookings.put(booking.id(), booking);
                        }
                    }
                    case BOOKING_CANCELLED -> {
                        UUID bookingId = UUID.fromString(rs.getString("booking_id"));
                        if (bookings.get(bookingId) != null) {
                            bookings.remove(bookingId);
                        } else {
                            throw new AssertionError("Booking not found for cancel event");
                        }
                    }
                    case ALL_BOOKINGS_CANCELLED -> {
                        bookings.clear();
                    }
                }
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
            UUID id = UUID.fromString(rs.getString("booking_id"));
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


    @Override
    public boolean addBooking(Booking booking) {
        String insertEvent = "INSERT INTO event_log (event_type, booking_id, apartment_id, from_date, to_date, who) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertEvent)) {

            stmt.setString(1, EventType.BOOKING_ADDED.toString());
            stmt.setString(2, booking.id().toString());
            stmt.setString(3, booking.apartmentID().toString());
            stmt.setDate(4, booking.fromDate());
            stmt.setDate(5, booking.toDate());
            stmt.setString(6, booking.who());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public @Nullable Booking getBookingById(UUID bookingId) {
        var state = buildState();
        return state.get(bookingId);
    }

    @Override
    public boolean cancelBooking(UUID bookingId) {
        String insertEvent = "INSERT INTO event_log (event_type, booking_id) VALUES (?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertEvent)) {

            stmt.setString(1, EventType.BOOKING_CANCELLED.toString());
            stmt.setString(2, bookingId.toString());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changeBookingDates(UUID bookingId, Date fromDate, Date toDate) {
        String insertEvent = "INSERT INTO event_log (event_type, booking_id, from_date, to_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertEvent)) {

            stmt.setString(1, EventType.BOOKING_CHANGED.toString());
            stmt.setString(2, bookingId.toString());
            stmt.setDate(3, fromDate);
            stmt.setDate(4, toDate);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void cancelAllBookings() {
        String insertEvent = "INSERT INTO event_log (event_type) VALUES (?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertEvent)) {

            stmt.setString(1, EventType.ALL_BOOKINGS_CANCELLED.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean rollbackToBooking(UUID bookingId) {
        //delete all entries AFTER the booking with this ID was ADDED
        // SQL to find the id of the booking created event
        String findBookingEventId = "SELECT id FROM event_log WHERE booking_id = ? AND event_type = ? LIMIT 1";

        // SQL to delete events after the found event id
        String deleteEvents = "DELETE FROM event_log WHERE id > ?";

        try (Connection conn = database.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findBookingEventId)) {

            // Set the booking ID and event type for the original booking event
            findStmt.setString(1, bookingId.toString());
            findStmt.setString(2, EventType.BOOKING_ADDED.toString());

            // Execute query to get the event id of the booking creation event
            try (ResultSet rs = findStmt.executeQuery()) {
                if (rs.next()) {
                    int bookingEventId = rs.getInt("id");

                    // Now delete events with ids greater than the booking event id
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteEvents)) {
                        deleteStmt.setInt(1, bookingEventId);
                        int rowsAffected = deleteStmt.executeUpdate();
                        return rowsAffected > 0; // Return true if any events were deleted
                    }
                } else {
                    // If no matching booking event is found, return false
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public List<Booking> listAllBookings() {
        return buildState().values().stream().toList();
    }

    @Override
    public JSONElement dumpBookings() {
        return dump("event_log");
    }

    private enum EventType {
        BOOKING_ADDED,
        BOOKING_CHANGED,
        BOOKING_CANCELLED,
        ALL_BOOKINGS_CANCELLED;

        public static EventType fromString(String eventType) {
            for (EventType type : EventType.values()) {
                if (type.name().equals(eventType)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid event type: " + eventType);
        }

        public String toString() {
            return this.name();
        }

    }
}
