package bookings;

import messages.*;

import java.sql.Date;
import java.util.UUID;

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

    public static void publishBookingAdded(Booking booking){
        INSTANCE.publishMessage(new BookingAddedMessage(booking.id(), booking.apartmentID(), booking.fromDate(), booking.toDate(), booking.who()));
    }

    public static void publishBookingChanged(UUID bookingId, Date fromDate, Date toDate){
        INSTANCE.publishMessage(new BookingChangedMessage(bookingId, fromDate, toDate));
    }

    public static void publishBookingCancelled(UUID bookingId){
        INSTANCE.publishMessage(new BookingCancelledMessage(bookingId));
    }

}
