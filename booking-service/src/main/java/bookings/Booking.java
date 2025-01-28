package bookings;

import java.sql.Date;
import java.util.Objects;
import java.util.UUID;

public record Booking(UUID id, UUID apartmentID, Date fromDate, Date toDate, String who) {

    public Booking(UUID apartmentID, Date fromDate, Date toDate, String who) {
        this(UUID.randomUUID(), Objects.requireNonNull(apartmentID), Objects.requireNonNull(fromDate), Objects.requireNonNull(toDate), Objects.requireNonNull(who));
    }
}
