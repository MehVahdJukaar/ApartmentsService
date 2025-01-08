package common;

import java.util.UUID;

public record ApartmentRemovedMessage(UUID apartmentId) implements Message {
    @Override
    public String getEventType() {
        return "ApartmentRemoved";
    }
}
