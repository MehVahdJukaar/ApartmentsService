package bookings;

import common.*;

import java.sql.Date;
import java.util.UUID;

public class BookingsMQService extends SimpleRabbitMQService {

    public static final BookingsMQService INSTANCE = new BookingsMQService();

    public static void initialize() {
    }

    BookingsMQService() {
        super("booking");
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message instanceof ApartmentAddedMessage added) {
            BookingsDAO.INSTANCE.addApartment(added.id(), added.name());
            System.out.println("Apartment added: " + added.name() + " at " + added.address());
        } else if (message instanceof ApartmentRemovedMessage removed) {
            BookingsDAO.INSTANCE.removeApartment(removed.apartmentId());
            System.out.println("Apartment removed: " + removed.apartmentId());
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
