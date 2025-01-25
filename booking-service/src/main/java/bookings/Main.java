package bookings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import common.Ports;
import common.StringMessage;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.sql.Date;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        System.out.println("Initializing Bookings...");

        boolean isEventSourcing = true;

        // Initialize the database (create the table if it doesn't exist)
        BookingsDAO.initialize(isEventSourcing);
        BookingsApi.initialize();


        BookingsMQService.initialize();

        // Publish a message to the MQ as an example
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
        System.out.println("Fetching apartments from the Apartment service...");
        HttpResponse<String> response = Unirest.get("http://" + Ports.APARTMENT_HOST + "/list")
                .asString();

        if (response.isSuccess()) {
            String body = response.getBody();

            // Parse the JSON response
            JsonArray apartments = JsonParser.parseString(body).getAsJsonArray();

            // Iterate through each apartment and print its id and name
            for (JsonElement element : apartments) {
                UUID id = UUID.fromString(element.getAsJsonObject().get("id").getAsString());
                String name = element.getAsJsonObject().get("name").getAsString();
                BookingsDAO.INSTANCE.addApartment(id, name);
                System.out.println("Fetched apartment: " + name);
            }
        } else {
            System.out.println("Failed to fetch apartments from the Apartment service.");
        }
    }
}