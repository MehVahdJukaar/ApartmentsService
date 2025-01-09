package gateway;

import common.Ports;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static spark.Spark.*;

public class GatewayApi {

    public static void initialize() throws IOException {
        ipAddress("0.0.0.0");  // Listen on all available network interfaces
        port(Ports.GATEWAY_PORT);

        // Default route
        get("/", (req, res) -> {
            res.status(200);  // OK
            return "Welcome to the Gateway Microservice!";
        });

        // Forwarding routes for all HTTP methods
        forwardRoute("/apartments/*", Ports.APARTMENT_HOST);
        forwardRoute("/bookings/*", Ports.BOOKING_HOST);
        forwardRoute("/search/*", Ports.SEARCH_HOST);
    }

    private static void forwardRoute(String path, String targetHost) {
        // Handle all HTTP methods for the given path
        get(path, (req, res) -> forward(req, res, targetHost));
        post(path, (req, res) -> forward(req, res, targetHost));
        put(path, (req, res) -> forward(req, res, targetHost));
        delete(path, (req, res) -> forward(req, res, targetHost));
        patch(path, (req, res) -> forward(req, res, targetHost));
    }

    private static String forward(Request req, Response res, String targetHost) {
        // Extract the original URL after "/forward"
        String[] splat = req.splat();
        String path = splat.length == 0 ? "" : splat[0];
        String targetUrl = "http://" + targetHost + "/" + path;
        System.out.println("Forwarding request to: " + targetUrl);

        // Get the HTTP method (GET, POST, PUT, DELETE, etc.)
        String method = req.requestMethod();

        // Prepare the Unirest request
        var request = Unirest.request(method, targetUrl);

        // Only set the body for methods that require it (POST, PUT, PATCH)
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            request.body(req.body());  // Forward the request body
        }

        // Send the request and get the response
        HttpResponse<byte[]> forwarded = request.asBytes();

        // Return the response from the target server
        res.status(forwarded.getStatus());
        return new String(forwarded.getBody());
    }


}
