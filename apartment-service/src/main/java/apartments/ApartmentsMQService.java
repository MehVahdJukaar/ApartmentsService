package apartments;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ApartmentsMQService {

    private static final String EXCHANGE_NAME = "events_exchange"; // Exchange to broadcast events
    private static final String QUEUE_NAME = "service_queue"; // Queue for service-specific events

    private static Connection CONNECTION;
    private static Channel CHANNEL;

    static {
        try {
            // Establish a connection and create a channel
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");  // Assuming RabbitMQ is running locally
            CONNECTION = factory.newConnection();
            CHANNEL = CONNECTION.createChannel();

            // Declare an exchange for event broadcasting (Fanout type for broadcasting messages to all queues bound to it)
            CHANNEL.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

            // Declare a queue for receiving events, this can be specific to each service
            CHANNEL.queueDeclare(QUEUE_NAME, true, false, false, null);

            // Bind the queue to the exchange, to receive any messages broadcasted to it
            CHANNEL.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();  // Handle the error appropriately
        }
    }

    // Static method to send an event to RabbitMQ
    public static void sendEvent(String message) {
        try {
            CHANNEL.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent: '" + message + "'");
        } catch (IOException e) {
            e.printStackTrace();  // Handle the error appropriately
        }
    }

    // Static method to listen for incoming events
    public static void initialize() {
        try {
            // Create a consumer to process messages
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received: '" + message + "'");
            };

            // Start listening for events in the queue
            CHANNEL.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();  // Handle the error appropriately
        }
    }

    // Static method to close the connection when done
    public static void close() {
        try {
            if (CHANNEL != null && CHANNEL.isOpen()) {
                CHANNEL.close();
            }
            if (CONNECTION != null && CONNECTION.isOpen()) {
                CONNECTION.close();
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();  // Handle the error appropriately
        }
    }
}