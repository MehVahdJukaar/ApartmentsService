package apartments;

import com.rabbitmq.client.*;
import messages.ApartmentAddedMessage;
import messages.ApartmentRemovedMessage;
import messages.Message;
import messages.SimpleRabbitMQService;

import java.io.IOException;

public class ApartmentsMQService extends SimpleRabbitMQService {
    public static final ApartmentsMQService INSTANCE = new ApartmentsMQService();

    public static void initialize(){}

    ApartmentsMQService() {
        super("apartments_queue");
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message instanceof ApartmentAddedMessage added) {
            System.out.println("Apartment added: " + added.apartmentId() + " at " + added.location());
        } else if (message instanceof ApartmentRemovedMessage removed) {
            System.out.println("Apartment removed: " + removed.apartmentId());
        } else {
            System.out.println("Unhandled message type: " + message.getClass().getName());
        }

        lastMessage = message.serialize();
    }

    public static String lastMessage = null;

}
