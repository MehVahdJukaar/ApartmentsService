package apartments;

import common.StringMessage;

public class Main {

    public static void main(String[] args) {
        System.out.println("Initializing Apartments...");

        // Initialize the database (create the table if it doesn't exist)
        ApartmentsDatabase.initialize();
        ApartmentApi.initialize();
        ApartmentsMQService.initialize();

        ApartmentDAO.removeAllApartments();
        // Publish a message to the MQ as an example
        ApartmentsMQService.INSTANCE.publishMessage(new StringMessage("Hello, World From Apartments!"));

        // Keep the application running until manually shut down
        // Here, we can wait for a specific signal or use a simple mechanism
        // to keep the app alive (like waiting for the server to be stopped).

        System.out.println("Application is running. Press Ctrl+C to stop.");

        // Keep the application running indefinitely
        while (true) {
            try {
                Thread.sleep(10000);  // Sleep for a short time, or adjust as needed
            } catch (InterruptedException e) {
                e.printStackTrace();
                // If interrupted, the application will stop
                break;
            }
        }
    }
}
