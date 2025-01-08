package common;

public class Ports {

    public static final int APARTMENT_PORT = System.getenv("APARTMENT_PORT") != null ?
            Integer.parseInt(System.getenv("APARTMENT_PORT")) : 8080;

    public static final int BOOKING_PORT = System.getenv("BOOKING_PORT") != null ?
            Integer.parseInt(System.getenv("BOOKING_PORT")) : 8081;

    public static final int SEARCH_PORT = System.getenv("SEARCH_PORT") != null ?
            Integer.parseInt(System.getenv("SEARCH_PORT")) : 8082;

    public static final int GATEWAY_PORT = System.getenv("GATEWAY_PORT") != null ?
            Integer.parseInt(System.getenv("GATEWAY_PORT")) : 8083;

    public static final String APARTMENT_HOST = "apartment-service:" + APARTMENT_PORT;
    public static final String BOOKING_HOST = "booking-service:" + BOOKING_PORT;
    public static final String SEARCH_HOST = "search-service:" + SEARCH_PORT;
    public static final String GATEWAY_HOST = "gateway-service:" + GATEWAY_PORT;
}
