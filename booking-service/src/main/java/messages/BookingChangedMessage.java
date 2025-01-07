package messages;

import java.sql.Date;
import java.util.UUID;

public record BookingChangedMessage(UUID id, Date fromDate, Date toDate) implements Message {

    @Override
    public String getEventType() {
        return "BookingChanged";
    }
}
