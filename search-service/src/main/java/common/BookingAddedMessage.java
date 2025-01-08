package common;

import java.sql.Date;
import java.util.UUID;

public record BookingAddedMessage(UUID id, UUID apartmentID, Date fromDate, Date toDate, String who) implements Message {

    @Override
    public String getEventType() {
        return "BookingAdded";
    }
}
