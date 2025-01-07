package search;

import messages.*;

import java.sql.Date;
import java.util.UUID;

public class SearchMQService extends SimpleRabbitMQService {

    public static final SearchMQService INSTANCE = new SearchMQService();

    public static void initialize() {
    }

    SearchMQService() {
        super("booking");
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message instanceof ApartmentAddedMessage added) {
            SearchDAO.addApartment(added.id(), added.name());
            System.out.println("Apartment added: " + added.name() + " at " + added.address());
        } else if (message instanceof ApartmentRemovedMessage removed) {
            SearchDAO.removeApartment(removed.apartmentId());
            System.out.println("Apartment removed: " + removed.apartmentId());
        } else if (message instanceof BookingAddedMessage bookingAdded) {
            SearchDAO.addBooking(bookingAdded.id(), bookingAdded.apartmentID(), bookingAdded.fromDate(), bookingAdded.toDate());
            System.out.println("Booking added: " + bookingAdded.id());
        } else if (message instanceof BookingCancelledMessage bookingCancelled) {
            SearchDAO.removeBooking(bookingCancelled.id());
            System.out.println("Booking cancelled: " + bookingCancelled.id());
        }
    }

}
