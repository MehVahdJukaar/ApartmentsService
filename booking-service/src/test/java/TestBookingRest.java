import bookings.BookingsApi;
import bookings.Main;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static spark.Spark.stop;

public class TestBookingRest {

    @BeforeAll
    public static void setUp() {
        // Start Spark server
        BookingsApi.initialize(Main.PORT);
        RestAssured.baseURI = "http://localhost:4567";
    }

    @AfterAll
    public static void tearDown() {
        // Stop the API server after tests
        stop();  // Spark method to stop the server
    }

    @Test
    public void testAddBooking() {
        UUID apartmentId = UUID.randomUUID();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        given()
                .queryParam("apartment", apartmentId.toString())
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
    public void testCancelBooking() {
        // Add a booking to cancel it
        UUID apartmentId = UUID.randomUUID();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        String bookingId = given()
                .queryParam("apartment", apartmentId.toString())
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
    public void testChangeBooking() {
        // Add a booking to change it
        UUID apartmentId = UUID.randomUUID();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        String bookingId = given()
                .queryParam("apartment", apartmentId.toString())
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
    public void testListBookings() {
        // Add a booking first to list it
        UUID apartmentId = UUID.randomUUID();
        String fromDate = "2024-12-01";
        String toDate = "2024-12-10";
        String who = "John Doe";

        given()
                .queryParam("apartment", apartmentId.toString())
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
    public void testAddInvalidBooking() {
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
    public void testCancelNonExistentBooking() {
        given()
                .queryParam("id", UUID.randomUUID().toString())
                .when()
                .delete("/cancel")
                .then()
                .statusCode(404)
                .body(equalTo("Booking not found!"));
    }
}
