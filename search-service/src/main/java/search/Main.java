package search;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Search...");

        // Initialize the database (create the table if it doesn't exist)
        SearchDatabase.initialize();
        SearchApi.initialize();
       // SearchMQService.initialize();

        // Publish a message to the MQ as an example
       // SearchMQService.INSTANCE.publishMessage(new StringMessage("Hello, World From Search!"));

        // Keep the application running until manually shut down
        // Here, we can wait for a specific signal or use a simple mechanism
        // to keep the app alive (like waiting for the server to be stopped).


        System.out.println("Application is running. Press Ctrl+C to stop.");

        // Keep the application running indefinitely
        while (true) {
            try {
                Thread.sleep(10000);  // Sleep for a short time, or adjust as needed
            } catch (InterruptedException e) {
                e.printStackTrace();
                // If interrupted, the application will stop
                break;
            }
        }
    }
}