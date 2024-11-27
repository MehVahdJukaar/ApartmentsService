package apartments;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        // Initialize the database (create the table if it doesn't exist)
        ApartmentsDatabase.initialize();

        // Adding an apartment (for testing)
        String name = "Sunset View";
        String address = "123 Sunset Blvd";
        int noiseLevel = 3;
        int floor = 5;

        Apartment apartment = new Apartment(name, address, noiseLevel, floor);
        boolean success = ApartmentDAO.addApartment(apartment);
        if (success) {
            System.out.println("Apartment added successfully.");
        } else {
            System.out.println("Failed to add apartment.");
        }

        // Retrieve the apartment by its ID
        // We'll assume we know the ID (from the first added apartment)
        UUID apartmentId = apartment.id();

        Apartment apartment2 = ApartmentDAO.getApartmentById(apartmentId);

        if (apartment2 != null) {
            System.out.println("Apartment retrieved: " + apartment2);
        } else {
            System.out.println("Apartment with ID " + apartmentId + " not found.");
        }
    }
}