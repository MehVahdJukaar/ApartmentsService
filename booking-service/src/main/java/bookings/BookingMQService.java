package bookings;

import messages.ApartmentAddedMessage;
import messages.ApartmentRemovedMessage;
import messages.Message;
import messages.SimpleRabbitMQService;

public class BookingMQService extends SimpleRabbitMQService {

    public static final BookingMQService INSTANCE = new BookingMQService();

    public static void initialize() {
    }

    BookingMQService() {
        super("booking");
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message instanceof ApartmentAddedMessage added) {
            BookingDAO.addApartment(added.id(), added.name());
            System.out.println("Apartment added: " + added.name() + " at " + added.address());
        } else if (message instanceof ApartmentRemovedMessage removed) {
            BookingDAO.removeApartment(removed.apartmentId());
            System.out.println("Apartment removed: " + removed.apartmentId());
        } else {
            System.out.println("Unhandled message type: " + message.getClass().getName());
        }
    }

}
