package apartments;

import common.ConsulService;
import common.StringMessage;

public class Main {

    public static final int PORT = System.getenv("APARTMENT_PORT") != null ?
            Integer.parseInt(System.getenv("APARTMENT_PORT")) : 8080;

    public static void main(String[] args) {
        System.out.println("Initializing Apartments...");

        ConsulService.registerService("apartments", "apartments-1",
                "apartment-service", PORT);
        ApartmentsDatabase.initialize();
        ApartmentsApi.initialize(PORT);
        ApartmentsMQService.initialize();

        // Publish a hello message to the message queue
        ApartmentsMQService.INSTANCE.publishMessage(new StringMessage("Hello, World From Apartments!"));

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
