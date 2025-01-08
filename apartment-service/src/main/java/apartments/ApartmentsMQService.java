package apartments;

import common.ApartmentAddedMessage;
import common.Message;
import common.SimpleRabbitMQService;

import java.util.UUID;

public class ApartmentsMQService extends SimpleRabbitMQService {
    public static final ApartmentsMQService INSTANCE = new ApartmentsMQService();

    public static void initialize() {
    }

    ApartmentsMQService() {
        super("apartments");
    }

    @Override
    public void onMessageReceived(Message message) {

    }

    public static void publishApartmentAdded(Apartment apartment) {
        INSTANCE.publishMessage(new ApartmentAddedMessage(
                apartment.id(),apartment.name(),  apartment.address(), apartment.noiseLevel(), apartment.floor()));
    }

    public static void publishApartmentRemoved(UUID apartmentId) {
        INSTANCE.publishMessage(new common.ApartmentRemovedMessage(apartmentId));
    }
}
