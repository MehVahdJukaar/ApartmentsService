package bookings;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {

        // Initialize the database (create the table if it doesn't exist)
        BookingsDatabase.initialize();
        BookingApi.initialize();
        BookingMQService.initialize();




        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}