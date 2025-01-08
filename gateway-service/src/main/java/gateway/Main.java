package gateway;


import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Gateway...");

        try {
            GatewayApi.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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