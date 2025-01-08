package gateway;


public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Gateway...");

        GatewayApi.initialize();


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