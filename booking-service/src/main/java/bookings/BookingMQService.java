package bookings;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import messages.ApartmentAddedMessage;
import messages.ApartmentRemovedMessage;
import messages.Message;
import messages.SimpleRabbitMQService;

import java.io.IOException;

public class BookingMQService extends SimpleRabbitMQService {

    public static final BookingMQService INSTANCE = new BookingMQService();

    public static void initialize(){}

    BookingMQService() {
        super("booking_queue");
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
    }

}
