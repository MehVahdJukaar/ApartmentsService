package messages;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

// simple MQ service that connects all services to the global exchange, 1 queue per service
public abstract class SimpleRabbitMQService {
    private static final String RABBITMQ_HOST = "rabbitmq"; // Name of the RabbitMQ container in the docker-compose file
    private static final String RABBIT_USERNAME = "user";
    private static final String RABBIT_PASSWORD = "password";

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    public static final String GLOBAL_EXCHANGE = "global_exchange";

    private final Connection connection;
    private final Channel channel;

    protected final String myQueue;

    public SimpleRabbitMQService(String myQueue) {
        this.myQueue = myQueue;
        System.out.println("Initializing RabbitMQ connection...");
        // Create a connection to the RabbitMQ server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setUsername(RABBIT_USERNAME);
        factory.setPassword(RABBIT_PASSWORD);

        int attempt = 0;
        boolean connected = false;
        Exception lastException = null;
        Connection connection = null;
        Channel channel = null;

        // Retry logic in case RabbitMQ isn't available immediately
        while (attempt < MAX_RETRIES && !connected) {
            try {
                // Attempt to create the connection
                connection = factory.newConnection();
                channel = connection.createChannel();

                System.out.println("RabbitMQ connection established!");

                setupChannel(channel);

                // Listen to incoming messages
                channel.basicConsume(this.myQueue, true, this::onMessageReceived,
                        consumerTag -> System.out.println("Consumer '" + consumerTag + "' has been canceled."));

                connected = true;  // Set flag to true if successful connection

            } catch (IOException | TimeoutException e) {
                attempt++;
                lastException = e;
                System.out.println("Attempt " + attempt + " failed to connect to RabbitMQ. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        this.connection = connection;
        this.channel = channel;

        // If connection attempts exhausted, throw an exception
        if (!connected) {
            throw new RuntimeException("Failed to connect to RabbitMQ. " + MAX_RETRIES + " attempts. Aborting...", lastException);
        }
    }


    public void setupChannel(Channel channel) throws IOException {
        // Declare a queue to receive messages
        channel.queueDeclare(myQueue, true, false, false, null);

        // All services have 1 fanout exchange
        channel.exchangeDeclare(GLOBAL_EXCHANGE, BuiltinExchangeType.FANOUT, true);

        // Binds my queue to the exchange
        channel.queueBind(myQueue, GLOBAL_EXCHANGE, "");
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
                System.out.println("RabbitMQ connection closed.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close connection", e);
        }
    }

    public void publishMessage(Message message) {
        try {
            String jsonMessage = message.serialize(); // Use the Message interface to serialize
            channel.basicPublish(GLOBAL_EXCHANGE, "", null,
                    jsonMessage.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent: " + jsonMessage);
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    private void onMessageReceived(String consumerTag, Delivery delivery) {
        String jsonMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);
        System.out.println(" [x] Received: " + jsonMessage);
        try {
            // Deserialize the message using the Message interface
            Message message = Message.deserialize(jsonMessage);

            onMessageReceived(message);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }

    public abstract void onMessageReceived(Message message);

}
