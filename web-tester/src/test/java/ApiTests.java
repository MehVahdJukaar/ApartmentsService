import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiTests {

    private static final String BASE_URL_BOOKING = "http://localhost:8081";
    private static final String BASE_URL_APARTMENT = "http://localhost:8080";
    private String apartmentId;

    // Helper method to perform a POST request
    private String sendPostRequest(String urlString, String params) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = params.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // First, add an apartment to the apartment API
        String response = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        assertTrue(response.contains("Apartment added successfully"));

        // Get the apartment ID from the response (assuming the response contains it in some format)
        // If the response is a JSON, parse it to extract the apartment ID
        // For simplicity, we're just assuming the ID is 1
        apartmentId = "1"; // This would normally be extracted from the response
    }

    // Testing the booking API

    @Test
    void testAddBooking_ValidApartment() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=1&from=20250101&to=20250105&who=JohnDoe");
        assertTrue(response.contains("Booking added successfully"));
    }

    @Test
    void testAddBooking_InvalidApartment() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=999&from=20250101&to=20250105&who=JohnDoe");
        assertTrue(response.contains("Apartment not found"));
    }

    @Test
    void testCancelBooking_Valid() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/cancel", "id=1");
        assertTrue(response.contains("Booking cancelled"));
    }

    @Test
    void testCancelBooking_Invalid() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/cancel", "id=999");
        assertTrue(response.contains("Booking not found"));
    }

    @Test
    void testChangeBooking_Valid() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/change", "id=1&from=20250110&to=20250112");
        assertTrue(response.contains("Booking changed"));
    }

    @Test
    void testChangeBooking_Invalid() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/change", "id=999&from=20250110&to=20250112");
        assertTrue(response.contains("Booking not found"));
    }

    @Test
    void testListBookings() throws Exception {
        URL url = new URL(BASE_URL_BOOKING + "/list");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            assertTrue(response.toString().contains("id"));
        }
    }

    // Testing the apartment API

    @Test
    void testAddApartment_Valid() throws Exception {
        String response = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        assertTrue(response.contains("Apartment added successfully"));
    }

    @Test
    void testAddApartment_MissingField() throws Exception {
        String response = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2");
        assertTrue(response.contains("Missing required field: floor"));
    }

    @Test
    void testRemoveApartment_Valid() throws Exception {
        String response = sendPostRequest(BASE_URL_APARTMENT + "/remove", "id=1");
        assertTrue(response.contains("Apartment removed"));
    }

    @Test
    void testRemoveApartment_Invalid() throws Exception {
        String response = sendPostRequest(BASE_URL_APARTMENT + "/remove", "id=999");
        assertTrue(response.contains("Apartment not found"));
    }

    @Test
    void testListApartments() throws Exception {
        URL url = new URL(BASE_URL_APARTMENT + "/list");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            assertTrue(response.toString().contains("id"));
        }
    }

    // Combined tests

    @Test
    void testBookingNonExistentApartment() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=999&from=20250101&to=20250105&who=JohnDoe");
        assertTrue(response.contains("Apartment not found"));
    }

    @Test
    void testCancelNonExistentBooking() throws Exception {
        String response = sendPostRequest(BASE_URL_BOOKING + "/cancel", "id=999");
        assertTrue(response.contains("Booking not found"));
    }
}