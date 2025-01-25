package bookings;

import kong.unirest.json.JSONElement;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface BookingDataAccess {
    // Method to add a booking
    boolean addBooking(Booking booking);

    // Method to retrieve a booking by its ID
    @Nullable
    Booking getBookingById(UUID bookingId);

    // Method to cancel a booking by ID
    boolean cancelBooking(UUID bookingId);

    // Method to change an existing booking by ID
    boolean changeBooking(UUID bookingId, Date newFromDate, Date newToDate);

    // Method to list all bookings
    List<Booking> listAllBookings();

    // Method to change the booking dates if the apartment is available
    boolean changeBookingDates(UUID bookingId, Date fromDate, Date toDate);

    void cancelAllBookings();

    void addApartment(UUID apartmentId, String name);

    boolean removeApartment(UUID apartmentId);

    // Get a list of all apartments from the apartments table
    List<UUID> listApartments();

    boolean getApartment(UUID apartmentId);

    JSONElement dumpApartments();

    public JSONElement dumpBookings();

}
