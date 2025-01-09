package gateway;

import common.Ports;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import spark.Request;
import spark.Response;

import static spark.Service.ignite;

public class ForwardTest {

    public static void main(String[] args) {
        // Initialize Server 2 on port 4568 using a separate Service instance
        spark.Service server2 = ignite();
        server2.port(4568);
        server2.get("/some/call", (req, res) -> {
            return "Response from Server 2";
        });
        server2.get("/", (req, res) -> {
            return "Hello from Server 2";
        });
        System.out.println("Server 2 is running on port 4568.");


        spark.Service server1 = ignite();

        server1.port(4567);

        // Default route
        server1.get("/", (req, res) -> {
            res.status(200);  // OK
            return "Welcome to server 1";
        });

        // Forwarding routes for all HTTP methods
       forwardRoute(server1, "/test/*", "localhost:4568");
    }

    private static void forwardRoute(spark.Service s, String path, String targetHost) {
        // Handle all HTTP methods for the given path
        s.get(path, (req, res) -> forward(req, res, targetHost));
        s.post(path, (req, res) -> forward(req, res, targetHost));
        s.put(path, (req, res) -> forward(req, res, targetHost));
        s.delete(path, (req, res) -> forward(req, res, targetHost));
        s.patch(path, (req, res) -> forward(req, res, targetHost));
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
