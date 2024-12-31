package apartments;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ApartmentsMQService {
    private static final String MY_QUEUE_NAME = "apartments_queue";
    private static final String MY_EXCHANGE = "apartments_exchange";
    private static final String BOOKING_EXCHANGE = "booking_exchange";

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds
    private static Connection connection;
    private static Channel channel;

    public static void initialize() {
        // Create a connection to the RabbitMQ server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");

        int attempt = 0;
        boolean connected = false;

        // Retry logic in case RabbitMQ isn't available immediately
        while (attempt < MAX_RETRIES && !connected) {
            try {
                // Attempt to create the connection
                connection = factory.newConnection();
                channel = connection.createChannel();

                // Declare a queue to receive messages
                channel.queueDeclare(MY_QUEUE_NAME, true, false, false, null);

                // Declare an exchange to send messages
                channel.exchangeDeclare(MY_EXCHANGE, BuiltinExchangeType.DIRECT, true);

                // Bind the queue to the booking exchange
                channel.queueBind(MY_QUEUE_NAME, BOOKING_EXCHANGE, "");

                // Log successful connection
                System.out.println("RabbitMQ connection established!");

                // Listen to incoming messages
                channel.basicConsume(MY_QUEUE_NAME, true, ApartmentsMQService::onMessageReceived,
                        consumerTag -> System.out.println("Consumer '" + consumerTag + "' has been canceled."));

                connected = true;  // Set flag to true if successful connection

            } catch (IOException | TimeoutException e) {
                attempt++;
                System.out.println("Attempt " + attempt + " failed to connect to RabbitMQ. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // If connection attempts exhausted, throw an exception
        if (!connected) {
            throw new RuntimeException("Failed to connect to RabbitMQ. " + MAX_RETRIES + " attempts. Aborting...");
        }
    }

    public static void publishMessage(String message) {
        try {
            // Publish the message to the exchange
            channel.basicPublish(MY_EXCHANGE, "", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent: '" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
                System.out.println("RabbitMQ connection closed.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close connection", e);
        }
    }

    private static void onMessageReceived(String consumerTag, Delivery delivery) {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        System.out.println(" [x] Received: '" + message + "'");
    }
}
