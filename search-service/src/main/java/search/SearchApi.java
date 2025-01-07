package search;

import java.util.List;
import java.util.UUID;

import static spark.Spark.*;

public class SearchApi {

    // Initialize the API
    public static void initialize() {
        ipAddress("0.0.0.0");  // Listen on all available network interfaces
        port(8082);

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

            // Call the method to get available apartments
            List<String> availableApartments = SearchDAO.getAvailableApartments(fromDate, toDate);

            // If no apartments found
            if (availableApartments.isEmpty()) {
                res.status(404);  // Not Found
                return "No available apartments found for the given date range.";
            }

            // Return the available apartments as a comma-separated list
            res.status(200);  // OK
            return String.join(", ", availableApartments);
        });
    }
}
