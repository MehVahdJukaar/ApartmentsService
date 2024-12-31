import apartments.ApartmentsDatabase;
import apartments.ApartmentsMQService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestApartmentsMQ {

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize the database before each test
        //ApartmentsMQService.initialize();
    }

    // Add your tests here

    @Test
    public void testMessageSending() {
        // Send a message to the queue
        //ApartmentsMQService.publishMessage("Hello, World!");
    }

}
