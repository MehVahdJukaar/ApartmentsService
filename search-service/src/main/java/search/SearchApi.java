package search;

import java.util.List;

import static spark.Spark.*;

public class SearchApi {

    // Initialize the API
    public static void initialize(int port) {
        ipAddress("0.0.0.0");  // Listen on all available network interfaces
        port(port);

        // Welcome message
        get("/", (req, res) -> {
            res.status(200);  // OK
            return "Welcome to the Search Microservice!";
        });
        // Search endpoint
        get("/search", (req, res) -> {
            // Get query parameters from the request
            String fromDate = req.queryParams("from");
            String toDate = req.queryParams("to");

            // Check if the 'from' and 'to' parameters are provided
            if (fromDate == null || toDate == null) {
                res.status(400); // Bad Request
                return "Both 'from' and 'to' query parameters are required!";
            }

            //validate dates
            if (fromDate.compareTo(toDate) > 0) {
                res.status(400); // Bad Request
                return "The 'from' date must be before the 'to' date!";
            }

            // Call the method to get available apartments
            List<String> availableApartments = SearchDAO.getAvailableApartments(fromDate, toDate);

            // Return the available apartments as a comma-separated list
            res.status(200);  // OK
            return String.join(", ", availableApartments);
        });

        // Debug

        get("/dump_apartments", (req, res) -> {
            res.status(200);
            res.type("application/json");
            return SearchDAO.dumpApartments();
        });

        get("/dump_bookings", (req, res) -> {
            res.status(200);
            res.type("application/json");
            return SearchDAO.dumpBookings();
        });
    }
}
