package apartments;

import java.util.UUID;

public record Apartment(UUID id, String name, String address, int noiseLevel, int floor) {

    public Apartment(String name, String address, int noiseLevel, int floor) {
        this(UUID.randomUUID(), name, address, noiseLevel, floor);
    }

}
