import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiTests {

    private static final String BASE_URL_BOOKING = "http://localhost:8081";
    private static final String BASE_URL_APARTMENT = "http://localhost:8080";

    private Pair<Integer, String> sendRequest(String urlString, String params, String method) throws IOException {
        URL url;
        if ("DELETE".equals(method)) {
            // Append query parameters directly to the URL for DELETE requests
            url = new URL(urlString + "?" + params);
        } else {
            url = new URL(urlString);
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);

        if ("POST".equals(method) || "DELETE".equals(method)) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if ("POST".equals(method) || "DELETE".equals(method)) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = params.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }
        } else if ("GET".equals(method)) {
            connection.setDoOutput(false);
        }

        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        }catch (Exception e){

        }

        return Pair.of(responseCode, response.toString());
    }

    public Pair<Integer, String> sendPostRequest(String urlString, String params) throws IOException {
        return sendRequest(urlString, params, "POST");
    }

    public Pair<Integer, String> sendGetRequest(String urlString) throws IOException {
        return sendRequest(urlString, "", "GET");
    }

    public Pair<Integer, String> sendDeleteRequest(String urlString, String params) throws IOException {
        return sendRequest(urlString, params, "DELETE");
    }

    @Test
    void testAddApartment_Valid() throws Exception {
        Pair<Integer, String> response = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment1&address=123 Street&noiselevel=2&floor=3");
        assertEquals(201, response.getKey());
    }

    @Test
    void testAddApartment_MissingField() throws Exception {
        Pair<Integer, String> response = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2");
        assertEquals(400, response.getKey());
    }

    @Test
    void testListApartments() throws Exception {
        // Add two apartments first
        sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Modern Apartment&address=456 Avenue&noiselevel=5&floor=5");

        // Now list apartments
        URL url = new URL(BASE_URL_APARTMENT + "/list");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Check that both apartments are listed
            assertTrue(response.toString().contains("Cozy Apartment"));
            assertTrue(response.toString().contains("Modern Apartment"));
        }
    }

    @Test
    void testRemoveApartment_Valid() throws Exception {
        // Add an apartment and get its UUID
        Pair<Integer, String> addResponse = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        assertEquals(201, addResponse.getKey());
        String apartmentId = addResponse.getRight(); // Assuming UUID is returned in response body

        // Remove the apartment using the UUID
        Pair<Integer, String> removeResponse = sendDeleteRequest(BASE_URL_APARTMENT + "/remove", "id=" + apartmentId);
        assertEquals(200, removeResponse.getKey());
    }

    @Test
    void testRemoveApartment_Invalid() throws Exception {
        // Try to remove an apartment that doesn't exist
        Pair<Integer, String> response = sendDeleteRequest(BASE_URL_APARTMENT + "/remove",
                "id="+ UUID.randomUUID());
        assertEquals(404, response.getKey());
    }

    @Test
    void testAddBooking_ValidApartment() throws Exception {
        // Add an apartment and get its UUID
        Pair<Integer, String> apartmentResponse = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        assertEquals(201, apartmentResponse.getKey());
        String apartmentId = apartmentResponse.getValue().trim(); // Assuming UUID is returned in response body

        // Book the apartment using the UUID
        Pair<Integer, String> bookingResponse = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=" + apartmentId + "&from=20250101&to=20250105&who=JohnDoe");
        assertEquals(201, bookingResponse.getKey());
    }

    @Test
    void testAddBooking_InvalidApartment() throws Exception {
        // Attempt to book a non-existent apartment (UUID 999)
        Pair<Integer, String> response = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=999&from=20250101&to=20250105&who=JohnDoe");
        assertEquals(404, response.getKey());
        assertTrue(response.getValue().contains("Apartment not found"));
    }

    @Test
    void testCancelBooking_Valid() throws Exception {
        // Add an apartment and get its UUID
        Pair<Integer, String> addResponse = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        assertEquals(200, addResponse.getKey());
        String apartmentId = addResponse.getValue().trim();

        // Book the apartment
        Pair<Integer, String> bookingResponse = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=" + apartmentId + "&from=20250101&to=20250105&who=JohnDoe");
        assertEquals(200, bookingResponse.getKey());
        assertTrue(bookingResponse.getValue().contains("Booking added successfully"));

        // Now cancel the booking using its ID
        Pair<Integer, String> cancelResponse = sendPostRequest(BASE_URL_BOOKING + "/cancel", "id=1");
        assertEquals(200, cancelResponse.getKey());
        assertTrue(cancelResponse.getValue().contains("Booking cancelled"));
    }

    @Test
    void testCancelBooking_Invalid() throws Exception {
        // Try canceling a non-existent booking (ID 999)
        Pair<Integer, String> response = sendPostRequest(BASE_URL_BOOKING + "/cancel", "id=999");
        assertEquals(404, response.getKey());
        assertTrue(response.getValue().contains("Booking not found"));
    }

    @Test
    void testChangeBooking_Valid() throws Exception {
        // Add an apartment and get its UUID
        Pair<Integer, String> addResponse = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        assertEquals(200, addResponse.getKey());
        String apartmentId = addResponse.getValue().trim();

        // Book the apartment
        Pair<Integer, String> bookingResponse = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=" + apartmentId + "&from=20250101&to=20250105&who=JohnDoe");
        assertEquals(200, bookingResponse.getKey());
        assertTrue(bookingResponse.getValue().contains("Booking added successfully"));

        // Change the booking dates
        Pair<Integer, String> changeResponse = sendPostRequest(BASE_URL_BOOKING + "/change", "id=1&from=20250110&to=20250115");
        assertEquals(200, changeResponse.getKey());
        assertTrue(changeResponse.getValue().contains("Booking changed"));
    }

    @Test
    void testChangeBooking_Invalid() throws Exception {
        // Try changing a non-existent booking (ID 999)
        Pair<Integer, String> response = sendPostRequest(BASE_URL_BOOKING + "/change", "id=999&from=20250110&to=20250112");
        assertEquals(404, response.getKey());
        assertTrue(response.getValue().contains("Booking not found"));
    }

    @Test
    void testBookingInteractionWithApartments() throws Exception {
        // Add apartments and get UUIDs
        Pair<Integer, String> apartment1Response = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Cozy Apartment&address=123 Street&noiselevel=2&floor=3");
        assertEquals(200, apartment1Response.getKey());
        String apartment1Id = apartment1Response.getValue().trim();

        Pair<Integer, String> apartment2Response = sendPostRequest(BASE_URL_APARTMENT + "/add", "name=Modern Apartment&address=456 Avenue&noiselevel=5&floor=5");
        assertEquals(200, apartment2Response.getKey());
        String apartment2Id = apartment2Response.getValue().trim();

        // Book a valid apartment
        Pair<Integer, String> bookingResponse = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=" + apartment1Id + "&from=20250101&to=20250105&who=JohnDoe");
        assertEquals(200, bookingResponse.getKey());
        assertTrue(bookingResponse.getValue().contains("Booking added successfully"));

        // Try booking a non-existent apartment
        Pair<Integer, String> invalidBookingResponse = sendPostRequest(BASE_URL_BOOKING + "/add", "apartment=999&from=20250101&to=20250105&who=JaneDoe");
        assertEquals(404, invalidBookingResponse.getKey());
        assertTrue(invalidBookingResponse.getValue().contains("Apartment not found"));

        // Try canceling a non-existent booking
        Pair<Integer, String> cancelResponse = sendPostRequest(BASE_URL_BOOKING + "/cancel", "id=999");
        assertEquals(404, cancelResponse.getKey());
        assertTrue(cancelResponse.getValue().contains("Booking not found"));

        // List bookings and ensure the valid booking appears
        URL url = new URL(BASE_URL_BOOKING + "/list");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder responseContent = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                responseContent.append(responseLine.trim());
            }
            assertTrue(responseContent.toString().contains("id"));
        }
    }
}
