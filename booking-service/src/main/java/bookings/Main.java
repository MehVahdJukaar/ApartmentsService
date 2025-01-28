package bookings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import common.ConsulService;
import common.StringMessage;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.UUID;

public class Main {

    public static final int PORT = System.getenv("BOOKING_PORT") != null ?
            Integer.parseInt(System.getenv("BOOKING_PORT")) : 8081;

    public static void main(String[] args) {
        System.out.println("Initializing Bookings...");

        // Default value for event sourcing
        boolean isEventSourcing = true;

        // Check if an argument for event sourcing is passed
        if (args.length > 0) {
            String eventSourcingArg = args[0].toLowerCase();
            if ("false".equals(eventSourcingArg)) {
                isEventSourcing = false;
            } else if (!"true".equals(eventSourcingArg)) {
                System.out.println("Invalid argument for event sourcing. Using default: true");
            }
        }

        ConsulService.registerService("bookings", "bookings-1",
                "booking-service", PORT);

        // Initialize the database (create the table if it doesn't exist)
        BookingsDAO.initialize(isEventSourcing);

        // Initialize the rest API
        BookingsApi.initialize(PORT);

        // Initialize the message queue service
        BookingsMQService.initialize();

        // Publish a hello world to the MQ
        BookingsMQService.INSTANCE.publishMessage(new StringMessage("Hello, World From Bookings!"));

        try {
            Thread.sleep(10000);  // Sleep for a short time so other services can start
            if (BookingsDAO.INSTANCE.listApartments().isEmpty()) {
                fetchApartmentsDirectly();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    public static void fetchApartmentsDirectly() {
        String apartmentsAddress = ConsulService.discoverServiceAddress("apartments");
        System.out.println("Fetching apartments from the Apartment service from " + apartmentsAddress);
        int tries = 0;
        while (tries < 10) {
            try {
                HttpResponse<String> response = Unirest.get("http://" + apartmentsAddress + "/list").asString();

                if (response.isSuccess()) {
                    String body = response.getBody();

                    // Parse the JSON response
                    JsonArray apartments = JsonParser.parseString(body).getAsJsonArray();
                    System.out.println("Apartments fetched: " + apartments);

                    // Iterate through each apartment and print its id and name
                    for (JsonElement element : apartments) {
                        UUID id = UUID.fromString(element.getAsJsonObject().get("id").getAsString());
                        String name = element.getAsJsonObject().get("name").getAsString();
                        BookingsDAO.INSTANCE.addApartment(id, name);
                    }
                    return;
                }else{
                    System.out.println("HTTP error while fetching apartments " + response);
                }
            } catch (Exception e) {
                System.out.println("Error occurred while fetching apartments" + e);
            }

            tries++;
            System.out.println("Failed to fetch apartments from the Apartment service. Attempt " + tries + " Retrying...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}