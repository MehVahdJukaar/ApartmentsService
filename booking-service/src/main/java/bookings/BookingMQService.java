package bookings;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class BookingMQService {
    private static final String MY_QUEUE_NAME = "bookings_queue";
    private static final String MY_EXCHANGE = "bookings_exchange";

    private static final String BOOKING_EXCHANGE = "apartments_exchange";

    // Connection and channel objects for interacting with RabbitMQ
    private static Connection connection;
    private static Channel channel;

    public static void initialize() {
        // Create a connection to the RabbitMQ server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            // no need to close. The connection will be closed when the application is closed
            connection = factory.newConnection();
            channel = connection.createChannel();
            // Declare a queue to receive messages
            channel.queueDeclare(MY_QUEUE_NAME, true, false, false, null);
            // Declare an exchange to send my messages
            channel.exchangeDeclare(MY_EXCHANGE, BuiltinExchangeType.DIRECT, true);

            // Listen to other services exchanges. No routing key is needed
            channel.queueBind(MY_QUEUE_NAME, BOOKING_EXCHANGE, "");

            // Define callback

            channel.basicConsume(MY_QUEUE_NAME, true, BookingMQService::onMessageReceived,
                    consumerTag -> {
                        System.out.println("Consumer '" + consumerTag + "' has been canceled.");
                    });

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to initialize RabbitMQ", e);
        }
    }


    public static void publishMessage(String message) {
        try {
            // Publish the message to the exchange using the routing key
            channel.basicPublish(MY_EXCHANGE, "", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent: '" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish message", e);
        }
    }


    // impl

    private static void onMessageReceived(String s, Delivery delivery) {
        System.out.println(" [x] Received: '" + new String(delivery.getBody(), StandardCharsets.UTF_8) + "'");
    }

}