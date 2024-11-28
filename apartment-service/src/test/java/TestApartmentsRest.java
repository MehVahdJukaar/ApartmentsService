import apartments.ApartmentApi;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static spark.Spark.stop;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestApartmentsRest {

    @BeforeAll
    public static void setUp() {
        // Start the API server
        ApartmentApi.initialize();
        RestAssured.baseURI = "http://localhost:4567";
    }

    @AfterAll
    public static void tearDown() {
        // Stop the API server after tests
        stop();  // Spark method to stop the server
    }

    @Test
    @Order(1)
    public void testAddApartment() {
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

    //@Test
    //@Order(3)
    public void testRemoveApartment() {
        // Replace with the actual UUID from your database or mock the UUID
        String apartmentId = "ReplaceWithActualID";

        given()
                .queryParam("id", apartmentId)
                .when()
                .delete("/remove")
                .then()
                .statusCode(200)  // Assert HTTP status code
                .body(equalTo("Apartment removed successfully!"));  // Assert response body
    }

    //@Test
    //@Order(4)
    public void testListEmpty() {
        when()
                .get("/list")
                .then()
                .statusCode(404)  // Assert HTTP status code
                .body(equalTo("No apartments found!"));  // Assert response body
    }
}

