package common;

import java.util.UUID;

public record ApartmentAddedMessage(UUID id, String name, String address, int noiseLevel, int floor) implements Message {

    @Override
    public String getEventType() {
        return "ApartmentAdded";
    }
}
