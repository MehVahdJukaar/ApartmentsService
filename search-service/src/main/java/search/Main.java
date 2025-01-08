package search;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.Ports;
import common.StringMessage;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.sql.Date;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Search...");

        // Initialize the database (create the table if it doesn't exist)
        SearchDatabase.initialize();
        SearchApi.initialize();

        SearchDAO.clearAllData();


        try {
            Thread.sleep(1000);  // Sleep for a short time so other services can start
            if (SearchDAO.getAllApartments().isEmpty()) {
                fetchApartmentsDirectly();
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
        HttpResponse<String> response = Unirest.get(Ports.APARTMENT_HOST + "/list")
                .asString();

        if (response.isSuccess()) {
            String body = response.getBody();

            // Parse the JSON response
            JsonArray apartments = JsonParser.parseString(body).getAsJsonArray();

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
        HttpResponse<String> response = Unirest.get(Ports.BOOKING_HOST + "/list")
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