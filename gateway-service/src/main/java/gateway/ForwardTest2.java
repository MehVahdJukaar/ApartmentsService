package gateway;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForwardTest2 {
    public static void main(String[] args) throws Exception {
        // Start the first server on port 4567 with 4 HTTP methods (GET, POST, DELETE, PUT)
        HttpServer server1 = HttpServer.create(new InetSocketAddress(4567), 0);
        server1.createContext("/", new Server1Handler());
        server1.start();
        System.out.println("Server 1 running on port 4567");

        // Start the second server on port 4568 that forwards requests to Server 1
        HttpServer server2 = HttpServer.create(new InetSocketAddress(4568), 0);
        server2.createContext("/", new Server2Handler());
        server2.start();
        System.out.println("Server 2 running on port 4568");
    }

    static class Server1Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Handled " + exchange.getRequestMethod() + " request.";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class Server2Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI requestUri = exchange.getRequestURI();
            String path = requestUri.getPath();

            if (path.startsWith("/forward/")) {
                // Forward the request to Server 1 (directly)
                String forwardPath = path.substring(9);  // Remove '/forward/'
                URI forwardUri = URI.create("http://localhost:4567" + forwardPath);

                // Forward the request and response from Server 1
                forwardRequest(forwardUri, exchange);
            } else {
                String response = "Invalid forward path.";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
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

            // Read the response body
            InputStream responseStream = connection.getInputStream();
            byte[] responseBody = responseStream.readAllBytes();

            // Send the response from Server 1 back to the client
            exchange.sendResponseHeaders(responseCode, responseBody.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody);
            }
        }
    }
}
