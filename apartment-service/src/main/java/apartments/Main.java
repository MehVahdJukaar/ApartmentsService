package apartments;

public class Main {

    public static void main(String[] args) {
        // Initialize the database (create the table if it doesn't exist)
        ApartmentsDatabase.initialize();
        ApartmentApi.initialize();
        ApartmentsMQService.initialize();

        ApartmentDAO.removeAllApartments();
        ApartmentDAO.addApartment(new Apartment("Apartment 1", "123 Main St", 1, 1));
        ApartmentDAO.addApartment(new Apartment("Apartment 2", "345 Main St", 0, -1));
        //sleep for 2m

        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}