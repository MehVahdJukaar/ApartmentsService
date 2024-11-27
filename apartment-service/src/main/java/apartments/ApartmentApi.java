package apartments;

import java.util.List;
import java.util.UUID;

import static spark.Spark.*;

public class ApartmentApi {

    // Initialize the API
    public static void initialize() {
        // Setup routes and endpoints

        // Endpoint to add a new apartment
        post("/add", (req, res) -> {
            String name = req.queryParams("name");
            String address = req.queryParams("address");
            int noiseLevel = Integer.parseInt(req.queryParams("noiselevel"));
            int floor = Integer.parseInt(req.queryParams("floor"));

            Apartment apartment = new Apartment(name, address, noiseLevel, floor);
            boolean success = ApartmentDAO.addApartment(apartment);

            if (success) {
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
            StringBuilder response = new StringBuilder();

            if (apartments.isEmpty()) {
                res.status(404);  // Not Found
                return "No apartments found!";
            }

            for (Apartment apartment : apartments) {
                response.append("ID: ").append(apartment.id())
                        .append(", Name: ").append(apartment.name())
                        .append(", Address: ").append(apartment.address())
                        .append(", Noise Level: ").append(apartment.noiseLevel())
                        .append(", Floor: ").append(apartment.floor())
                        .append("\n");
            }

            res.type("text/plain");
            return response.toString();  // Return apartment list as plain text
        });
    }
}
