import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

public class ApiTests {

    private static final String BASE_URL_BOOKING = "http://localhost:8081";
    private static final String BASE_URL_APARTMENT = "http://localhost:8080";

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = BASE_URL_BOOKING;
        //reset all bookings
        given()
                .when()
                .delete("/cancel_all")
                .then()
                .statusCode(200);
        RestAssured.baseURI = BASE_URL_APARTMENT;
        //reset all apartments
        given()
                .when()
                .delete("/remove_all")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(1)
    public void testAddApartment() {
        RestAssured.baseURI = BASE_URL_APARTMENT;
        given()
                .queryParam("name", "Apartment 101")
                .queryParam("address", "123 Main St")
                .queryParam("noiselevel", 3)
                .queryParam("floor", 2)
                .when()
                .post("/add")
                .then()
                .statusCode(201);  // Assert HTTP status code
    }

    @Test
    @Order(2)
    public void testListApartments() {
        Response response =
                when()
                        .get("/list")
                        .then()
                        .statusCode(200)  // Assert HTTP status code
                        .extract().response();

        // Ensure response contains the added apartment
        String body = response.getBody().asString();
        Assertions.assertTrue(body.contains("Apartment 101"));
    }

    @Test
    @Order(3)
    public void testRemoveApartment() {
        // Replace with the actual UUID from your database or mock the UUID
        String apartmentId = addApartment();

        given()
                .queryParam("id", apartmentId)
                .when()
                .delete("/remove")
                .then()
                .statusCode(200);
    }

    private static String addApartment() {
        String oldBaseUri = RestAssured.baseURI;
        RestAssured.baseURI = BASE_URL_APARTMENT;
        AtomicReference<String> apartmentId = new AtomicReference<>();

        given()
                .queryParam("name", "Apartment 101")
                .queryParam("address", "123 Main St")
                .queryParam("noiselevel", 3)
                .queryParam("floor", 2)
                .when()
                .post("/add")
                .then()
                .body(new BaseMatcher<String>() {

                    @Override
                    public void describeTo(Description description) {

                    }

                    @Override
                    public boolean matches(Object o) {
                        apartmentId.set(o.toString());
                        return true;
                    }
                });  // Assert HTTP status code
        RestAssured.baseURI = oldBaseUri;
        return apartmentId.get();
    }

    @Test
    @Order(4)
    public void testListEmpty() {
        when()
                .get("/list")
                .then()
                .statusCode(404)  // Assert HTTP status code
                .body(equalTo("No apartments found!"));  // Assert response body
    }

    @Test
    @Order(5)
    public void testAddBooking() {
        RestAssured.baseURI = BASE_URL_BOOKING;

        String apartmentId = addApartment();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        given()
                .queryParam("apartment", apartmentId)
                .queryParam("from", fromDate)
                .queryParam("to", toDate)
                .queryParam("who", who)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .body(matchesPattern(".+"));  // Check if ID is returned
    }

    @Test
    @Order(6)
    public void testCancelBooking() {
        RestAssured.baseURI = BASE_URL_BOOKING;
        // Add a booking to cancel it
        String apartmentId = addApartment();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        String bookingId = given()
                .queryParam("apartment", apartmentId)
                .queryParam("from", fromDate)
                .queryParam("to", toDate)
                .queryParam("who", who)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .extract().body().asString();

        // Now cancel the booking
        given()
                .queryParam("id", bookingId)
                .when()
                .delete("/cancel")
                .then()
                .statusCode(200)
                .body(equalTo("Booking canceled successfully!"));
    }

    @Test
    @Order(7)
    public void testChangeBooking() {
        RestAssured.baseURI = BASE_URL_BOOKING;
        // Add a booking to change it
        String apartmentId = addApartment();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        String bookingId = given()
                .queryParam("apartment", apartmentId)
                .queryParam("from", fromDate)
                .queryParam("to", toDate)
                .queryParam("who", who)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .extract().body().asString();

        // Change the booking dates
        String newFromDate = "2024-12-05";
        String newToDate = "2024-12-15";

        given()
                .queryParam("id", bookingId)
                .queryParam("from", newFromDate)
                .queryParam("to", newToDate)
                .when()
                .post("/change")
                .then()
                .statusCode(200)
                .body(equalTo("Booking changed successfully!"));
    }

    @Test
    @Order(8)
    public void testListBookings() {
        // Add a booking first to list it
        String apartmentId = addApartment();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        given()
                .queryParam("apartment", apartmentId)
                .queryParam("from", fromDate)
                .queryParam("to", toDate)
                .queryParam("who", who)
                .when()
                .post("/add")
                .then()
                .statusCode(201);

        // Now list the bookings
        given()
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body(containsString("Booking ID:"));
    }

    @Test
    @Order(9)
    public void testAddInvalidBooking() {
        RestAssured.baseURI = BASE_URL_BOOKING;

        given()
                .queryParam("apartment", "invalid-uuid")
                .queryParam("from", "2024-12-01")
                .queryParam("to", "2024-12-10")
                .queryParam("who", "John Doe")
                .when()
                .post("/add")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid parameters!"));
    }

    @Test
    @Order(10)
    public void testCancelNonExistentBooking() {
        given()
                .queryParam("id", UUID.randomUUID().toString())
                .when()
                .delete("/cancel")
                .then()
                .statusCode(404);
    }
}
