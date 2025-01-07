import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class TestBooking {

    private static final String BASE_URL = "http://localhost:8081";

    public static void main(String[] args) throws Exception {
        testWelcomeEndpoint();
        testAddBookingEndpoint();
        testListBookingsEndpoint();
        testChangeBookingEndpoint();
        testCancelBookingEndpoint();
    }

    private static void testWelcomeEndpoint() throws Exception {
        System.out.println("Testing Welcome Endpoint...");
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response: " + readResponse(connection));
    }

    private static void testAddBookingEndpoint() throws Exception {
        System.out.println("\nTesting Add Booking Endpoint...");
        String params = "apartment=" + UUID.randomUUID() +
                "&from=2025-01-10" +
                "&to=2025-01-15" +
                "&who=John Doe";

        HttpURLConnection connection = createPostConnection("/add", params);

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response: " + readResponse(connection));
    }

    private static void testListBookingsEndpoint() throws Exception {
        System.out.println("\nTesting List Bookings Endpoint...");
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/list").openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response: " + readResponse(connection));
    }

    private static void testChangeBookingEndpoint() throws Exception {
        System.out.println("\nTesting Change Booking Endpoint...");
        String params = "id=" + UUID.randomUUID() +
                "&from=2025-02-01" +
                "&to=2025-02-10";

        HttpURLConnection connection = createPostConnection("/change", params);

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response: " + readResponse(connection));
    }

    private static void testCancelBookingEndpoint() throws Exception {
        System.out.println("\nTesting Cancel Booking Endpoint...");
        String params = "id=" + UUID.randomUUID();

        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/cancel?id=" + params).openConnection();
        connection.setRequestMethod("DELETE");

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response: " + readResponse(connection));
    }

    private static HttpURLConnection createPostConnection(String endpoint, String params) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + endpoint).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(params.getBytes());
            os.flush();
        }

        return connection;
    }

    private static String readResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString().trim();
        } catch (Exception e) {
            return "Error reading response: " + e.getMessage();
        }
    }
}
