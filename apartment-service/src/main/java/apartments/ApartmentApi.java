package apartments;

import com.google.gson.Gson;
import common.Ports;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.*;

public class ApartmentApi {

    // Initialize the API
    public static void initialize() {
        ipAddress("0.0.0.0");  // Listen on all available network interfaces
        port(Ports.APARTMENT_PORT);

        Gson gson = new Gson();
        // Welcome message
        get("/", (req, res) -> {
            res.status(200);  // OK
            return "Welcome to the Apartments Microservice!";
        });

        // Endpoint to add a new apartment
        post("/add", (req, res) -> {
            String name = req.queryParams("name");
            String address = req.queryParams("address");
            int noiseLevel = Integer.parseInt(req.queryParams("noiselevel"));
            int floor = Integer.parseInt(req.queryParams("floor"));

            Apartment apartment = new Apartment(name, address, noiseLevel, floor);
            boolean success = ApartmentDAO.addApartment(apartment);

            if (success) {
                ApartmentsMQService.publishApartmentAdded(apartment);
                res.status(201);  // Set HTTP response code as "Created"
                return apartment.id().toString(); // Return the UUID
            } else {
                res.status(400);  // Bad Request
                return "Failed to add apartment!";
            }
        });

        // Endpoint to remove an apartment by its ID
        delete("/remove", (req, res) -> {
            UUID apartmentId;
            try {
                apartmentId = UUID.fromString(req.queryParams("id"));
            } catch (IllegalArgumentException e) {
                res.status(400);  // Bad Request
                return "Invalid apartment ID!";
            }

            boolean success = ApartmentDAO.removeApartmentById(apartmentId);
            if (success) {
                ApartmentsMQService.publishApartmentRemoved(apartmentId);
                res.status(200);  // OK
                return "Apartment removed successfully!";
            } else {
                res.status(404);  // Not Found
                return "Apartment not found!";
            }
        });

        // Endpoint to list all apartments
        get("/list", (req, res) -> {
            List<Apartment> apartments = ApartmentDAO.getAllApartments();

            res.type("application/json");
            res.status(200);  // OK
            return gson.toJson(apartments);  // Serialize the list of apartments to JSON
        });

        // Remove all
        delete("/remove_all", (req, res) -> {
            ApartmentDAO.removeAllApartments();
            res.status(200);  // OK
            return "All apartments removed successfully!";
        });
    }
}
