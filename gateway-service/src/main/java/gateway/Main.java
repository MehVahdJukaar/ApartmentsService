package gateway;


import common.ConsulService;

public class Main {

    public static final int PORT = System.getenv("GATEWAY_PORT") != null ?
            Integer.parseInt(System.getenv("GATEWAY_PORT")) : 8083;

    public static void main(String[] args) {
        System.out.println("Initializing Gateway...");

        ConsulService.registerService("gateway", "gateway-1",
                "gateway-service", PORT);
        GatewayApi.initialize(PORT);

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