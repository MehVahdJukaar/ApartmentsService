package messages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;

public interface Message {
    String getEventType();

    Gson GSON = new Gson(); // Shared Gson instance
    Map<String, Class<? extends Message>> MESSAGE_REGISTRY = Map.of(
            "ApartmentAdded", ApartmentAddedMessage.class,
            "ApartmentRemoved", ApartmentRemovedMessage.class,
            "String", StringMessage.class
    );

    /**
     * Serializes the message to JSON.
     *
     * @return the JSON representation of the message
     */
    default String serialize() {
        // Convert this object to a JSON string
        String json = GSON.toJson(this);

        // Parse it back to a JsonObject to add the eventType field
        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
        jsonObject.addProperty("messageType", getEventType());

        // Convert back to string
        return jsonObject.toString();
    }

    /**
     * Deserializes a JSON string into the appropriate message type.
     *
     * @param json the JSON string
     * @return the deserialized message object
     * @throws IllegalArgumentException if the event type is not registered
     */
    static Message deserialize(String json) {
        var jsonObject = GSON.fromJson(json, JsonObject.class); // Parse as a JsonObject
        String eventType = jsonObject.get("messageType").getAsString();

        // Find the corresponding class for the event type
        Class<? extends Message> clazz = MESSAGE_REGISTRY.get(eventType);
        if (clazz == null) {
            throw new IllegalArgumentException("Unknown message type: " + eventType);
        }

        // Deserialize the JSON into the appropriate class
        return GSON.fromJson(json, clazz);
    }
}
