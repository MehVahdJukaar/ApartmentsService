package messages;

import java.util.UUID;

public record BookingCancelledMessage(UUID id) implements Message {

    @Override
    public String getEventType() {
        return "bookingCancelled";
    }
}
