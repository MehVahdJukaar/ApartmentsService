package search;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.ConsulService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.sql.Date;
import java.util.UUID;

public class Main {

    public static final int PORT = System.getenv("SEARCH_PORT") != null ?
            Integer.parseInt(System.getenv("SEARCH_PORT")) : 8082;

    public static void main(String[] args) {
        System.out.println("Initializing Search...");

        ConsulService.registerService("search", "search-1", PORT);
        // Initialize the database (create the table if it doesn't exist)
        SearchDatabase.initialize();
        SearchApi.initialize(PORT);
        SearchMQService.initialize();

        SearchDAO.clearAllData();


        try {
            Thread.sleep(1000);  // Sleep for a short time so other services can start
            if (SearchDAO.getAllApartments().isEmpty()) {
                fetchApartmentsDirectly();
            } else {
                System.out.println("Apartments already fetched" + SearchDAO.getAllApartments());
            }
            if (SearchDAO.getAllBookings().isEmpty()) {
                fetchBookingsDirectly();
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
        HttpResponse<String> response = Unirest.get("http://" +
                        ConsulService.discoverServiceAddress("apartments") + "/list")
                .asString();

        if (response.isSuccess()) {
            String body = response.getBody();

            // Parse the JSON response
            JsonArray apartments = JsonParser.parseString(body).getAsJsonArray();
            System.out.println("Apartments fetched: " + apartments);

            // Iterate through each apartment and print its id and name
            for (JsonElement element : apartments) {
                UUID id = UUID.fromString(element.getAsJsonObject().get("id").getAsString());
                String name = element.getAsJsonObject().get("name").getAsString();
                SearchDAO.addApartment(id, name);
                System.out.println("Fetched apartment: " + name);
            }
        } else {
            System.out.println("Failed to fetch apartments from the Apartment service.");
        }
    }

    public static void fetchBookingsDirectly() {
        System.out.println("Fetching bookings from the Booking service...");
        HttpResponse<String> response = Unirest.get("http://" +
                        ConsulService.discoverServiceAddress("bookings") + "/list")
                .asString();

        if (response.isSuccess()) {
            String body = response.getBody();

            // Parse the JSON response
            JsonArray bookings = JsonParser.parseString(body).getAsJsonArray();

            // Iterate through each booking and print its id and name
            for (JsonElement element : bookings) {
                JsonObject obj = element.getAsJsonObject();
                UUID id = UUID.fromString(obj.get("id").getAsString());
                UUID apartmentId = UUID.fromString(obj.get("apartmentID").getAsString());
                Date fromDate = Date.valueOf(obj.get("fromDate").getAsString());
                Date toDate = Date.valueOf(obj.get("toDate").getAsString());
                SearchDAO.addBooking(id, apartmentId, fromDate, toDate);
                System.out.println("Fetched booking: " + id);
            }
        } else {
            System.out.println("Failed to fetch bookings from the Booking service.");
        }
    }
}