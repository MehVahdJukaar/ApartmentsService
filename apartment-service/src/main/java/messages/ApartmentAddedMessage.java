package messages;

public record ApartmentAddedMessage(String eventType, String apartmentId, String location) implements Message {
    public ApartmentAddedMessage(String apartmentId, String location) {
        this("ApartmentAdded", apartmentId, location);
    }

    @Override
    public String getEventType() {
        return "apartmentAdded";
    }
}
