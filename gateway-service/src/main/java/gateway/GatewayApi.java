package gateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import common.ConsulService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

public class GatewayApi {

    public static void initialize(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/", new ForwardHandler());
            server.start();
            System.out.println("Gateway running on port " + port);
        } catch (IOException e) {
            System.out.println("Error starting Gateway: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class ForwardHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Handling request");

            // checks with Consul to get the address of the services
            Map<String, String> forwardMappings = Map.of(
                    "apartments", ConsulService.discoverServiceAddress("apartments"),
                    "bookings", ConsulService.discoverServiceAddress("bookings"),
                    "search", ConsulService.discoverServiceAddress("search")
            );

            URI requestUri = exchange.getRequestURI();
            String path = requestUri.getPath();

            // Iterate over forward mappings to check if the path matches any prefix
            for (var e : forwardMappings.entrySet()) {
                String prefix = e.getKey();
                if (path.startsWith("/" + prefix)) {
                    // Forward the request to the corresponding server based on the prefix
                    String forwardPath = path.substring(prefix.length() + 1);  // Remove '/{prefix}/'
                    URI forwardUri = URI.create("http://" + e.getValue() + forwardPath);
                    System.out.println("Forwarding request to: " + forwardUri);

                    // Forward the request and response from the respective server
                    try {
                        forwardRequest(forwardUri, exchange);
                    } catch (IOException ioException) {
                        System.err.println("Error forwarding request to " + forwardUri + ": " + ioException.getMessage());
                        ioException.printStackTrace();

                        // Respond with an internal server error (HTTP 502 Bad Gateway)
                        exchange.sendResponseHeaders(502, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(("Error forwarding request: " + ioException.getMessage()).getBytes());
                        }
                    }
                    return;
                }
            }

            // If no matching prefix found, respond with an error
            String response = "Unknown Service " + path + ". Available services: " + forwardMappings.keySet();
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private void forwardRequest(URI forwardUri, HttpExchange exchange) throws IOException {
            // Open connection to Server 1
            HttpURLConnection connection = (HttpURLConnection) forwardUri.toURL().openConnection();

            connection.setRequestMethod(exchange.getRequestMethod());

            connection.setDoOutput(true);

            // Copy headers from the incoming request
            exchange.getRequestHeaders().forEach((key, value) -> connection.setRequestProperty(key, String.join(",", value)));

            // Copy the request body if it's a POST or PUT
            if ("POST".equals(exchange.getRequestMethod()) || "PUT".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    connection.getOutputStream().write(is.readAllBytes());

                }
            }
            // Get the response from Server 1
            int responseCode = connection.getResponseCode();
            byte[] responseBody;
            try {
                InputStream responseStream = connection.getInputStream();
                responseBody = responseStream.readAllBytes();
            } catch (IOException e) {
                responseBody = new byte[0];
            }

            // Send the response from Server 1 back to the client
            exchange.sendResponseHeaders(responseCode, responseBody.length);
            if (responseBody.length > 0) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody);
                }
            }
        }
    }


}
