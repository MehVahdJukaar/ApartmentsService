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
}
