package gateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import common.Ports;
import kong.unirest.Header;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.stream.Collectors;

public class GatewayApi {

    public static void initialize() throws IOException {
        int port = (Ports.GATEWAY_PORT);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Welcome route
        server.createContext("/", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 200, "Welcome to the Gateway Microservice!");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        });

        // Bookings route
        server.createContext("/bookings/", new ForwardingHandler(Ports.BOOKING_HOST));

        // Apartments route
        server.createContext("/apartments/", new ForwardingHandler(Ports.APARTMENT_HOST));

        // Search route
        server.createContext("/search/", new ForwardingHandler(Ports.SEARCH_HOST));

        System.out.println("Gateway server is running on port " + port);
        server.start();
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private record ForwardingHandler(String host) implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String original = exchange.getRequestURI().toString();

            try {
                String uri = "http://" + host + original.substring(0, original.indexOf("/"));
                System.out.println("Forwarding request to: " + uri);

                // Extract headers from the incoming request
                var headers = exchange.getRequestHeaders().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> String.join(",", entry.getValue())
                        ));

                // Prepare the Unirest request
                var request = Unirest.request(method, uri)
                        .headers(headers);

                // Add request body if necessary
                if (!"GET".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
                    byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
                    request.body(new String(bodyBytes));
                }

                // Execute the request
                HttpResponse<String> response = request.asString();

                // Forward the response back to the client
                exchange.getResponseHeaders().putAll(response.getHeaders().all().stream()
                        .collect(Collectors.groupingBy(
                                Header::getName,
                                Collectors.mapping(Header::getValue, Collectors.toList())
                        )));
                sendResponse(exchange, response.getStatus(), response.getBody());
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Error forwarding request: " + e.getMessage());
            }
        }
    }
}
