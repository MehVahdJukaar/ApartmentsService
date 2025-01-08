package search;

import common.Ports;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class GatewayApi {

    // Initialize the API
    public static void initialize() {
        ipAddress("0.0.0.0");  // Listen on all available network interfaces
        port(Ports.GATEWAY_PORT);

        // Welcome message
        get("/", (req, res) -> {
            res.status(200);  // OK
            return "Welcome to the Gateway Microservice!";
        });
        // Search endpoint
        get("/search", (req, res) -> {
            //forward to search service
            return forwardRequest(Ports.SEARCH_PORT, req, res);
        });
        // Apartment endpoint
        post("/add", (req, res) -> {
            //forward to apartment service
            return forwardRequest(Ports.APARTMENT_PORT, req, res);
        });
        delete("/remove", (req, res) -> {
            //forward to apartment service
            return forwardRequest(Ports.APARTMENT_PORT, req, res);
        });
        get("/list", (req, res) -> {
            //forward to apartment service
            return forwardRequest(Ports.APARTMENT_PORT, req, res);
        });
        delete("/remove_all", (req, res) -> {
            //forward to apartment service
            return forwardRequest(Ports.APARTMENT_PORT, req, res);
        });
        // Booking endpoint
        post("/add", (req, res) -> {
            //forward to booking service
            return forwardRequest(Ports.BOOKING_PORT, req, res);
        });
        delete("/cancel", (req, res) -> {
            //forward to booking service
            return forwardRequest(Ports.BOOKING_PORT, req, res);
        });
        get("/list", (req, res) -> {
            //forward to booking service
            return forwardRequest(Ports.BOOKING_PORT, req, res);
        });
        post("/change", (req, res) -> {
            //forward to booking service
            return forwardRequest(Ports.BOOKING_PORT, req, res);
        });
        delete("/cancel_all", (req, res) -> {
            //forward to booking service
            return forwardRequest(Ports.BOOKING_PORT, req, res);
        });
    }

    // Method to forward the request to localhost:8080
    private static String forwardRequest(int port, Request req, Response res) {
        String method = req.requestMethod();
        String uri = "http://localhost:" + port;

        try {
            // Convert Spark headers (Set) to a Map for Unirest
            Map<String, String> headers = convertHeaders(req);

            // Use Unirest to forward the request
            HttpResponse<String> response = Unirest.request(method, uri)
                    .body(req.body())
                    .headers(headers)
                    .asString();

            // Set the response from localhost:8080 to the original request
            res.status(response.getStatus());
            for (var header : response.getHeaders().all()) {
                res.header(header.getName(), header.getValue());
            }
            return response.getBody();
        } catch (Exception e) {
            res.status(500);
            return "Error forwarding request: " + e.getMessage();
        }
    }

    // Helper method to convert Spark headers (Set) to a Map for Unirest
    private static Map<String, String> convertHeaders(spark.Request req) {
        Map<String, String> headersMap = new HashMap<>();
        for (String headerName : req.headers()) {
            // Spark headers contain multiple values for a single key, so join them with commas
            String headerValue = String.join(",", req.headers(headerName));
            headersMap.put(headerName, headerValue);
        }
        return headersMap;
    }
}
