package messages;

public record ApartmentRemovedMessage(String eventType, String apartmentId) implements Message {
    public ApartmentRemovedMessage(String apartmentId) {
        this("ApartmentRemoved", apartmentId);
    }

    @Override
    public String getEventType() {
        return "apartmentRemoved";
    }
}
