package bookings;

import java.sql.Date;
import java.util.UUID;

public record Booking(UUID id, UUID apartmentID, Date fromDate, Date toDate, String who) {

    public Booking(UUID apartmentID, Date fromDate, Date toDate, String who) {
        this(UUID.randomUUID(), apartmentID, fromDate, toDate, who);
    }
}
