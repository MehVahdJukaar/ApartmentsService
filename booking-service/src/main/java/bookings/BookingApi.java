package bookings;

import bookings.Booking;
import bookings.BookingDAO;
import com.google.gson.Gson;
import common.Ports;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.*;

public class BookingApi {

    // Initialize the API
    public static void initialize() {
        ipAddress("0.0.0.0");
        port(Ports.BOOKING_PORT);
        Gson gson = new Gson();

        // Welcome message
        get("/", (req, res) -> {
            res.status(200);  // OK
            return "Welcome to the Booking Microservice!";
        });

        // Endpoint to add a new booking
        post("/add", (req, res) -> {
            UUID apartmentId;
            Date fromDate;
            Date toDate;
            String who;

            try {
                apartmentId = UUID.fromString(req.queryParams("apartment"));
                fromDate = Date.valueOf(req.queryParams("from")); // Assuming format is yyyy-mm-dd
                toDate = Date.valueOf(req.queryParams("to"));     // Assuming format is yyyy-mm-dd
                who = req.queryParams("who");
            } catch (Exception e) {
                res.status(400);  // Bad Request
                return "Invalid parameters!";
            }

            // Create new booking object
            Booking booking = new Booking(apartmentId, fromDate, toDate, who);
            boolean success = BookingDAO.addBooking(booking);

            if (success) {
                BookingMQService.publishBookingAdded(booking);
                res.status(201);  // Created
                return booking.id();
            } else {
                res.status(404);  // No apartment
                return "No apartment found for booking!";
            }
        });

        // Endpoint to cancel a booking
        delete("/cancel", (req, res) -> {
            UUID bookingId;
            try {
                bookingId = UUID.fromString(req.queryParams("id"));
            } catch (IllegalArgumentException e) {
                res.status(400);  // Bad Request
                return "Invalid booking ID!";
            }

            boolean success = BookingDAO.cancelBooking(bookingId);
            if (success) {
                BookingMQService.publishBookingCancelled(bookingId);
                res.status(200);  // OK
                return "Booking canceled successfully!";
            } else {
                res.status(404);  // Not Found
                return "Booking not found!";
            }
        });

        // Endpoint to change a booking
        post("/change", (req, res) -> {
            UUID bookingId;
            Date fromDate;
            Date toDate;

            try {
                bookingId = UUID.fromString(req.queryParams("id"));
                fromDate = Date.valueOf(req.queryParams("from")); // Assuming format is yyyy-mm-dd
                toDate = Date.valueOf(req.queryParams("to"));     // Assuming format is yyyy-mm-dd
            } catch (Exception e) {
                res.status(400);  // Bad Request
                return "Invalid parameters!";
            }

            boolean success = BookingDAO.changeBookingDates(bookingId, fromDate, toDate);
            if (success) {
                BookingMQService.publishBookingChanged(bookingId, fromDate, toDate);
                res.status(200);  // OK
                return "Booking changed successfully!";
            } else {
                res.status(404);  // Not Found
                return "Booking not found!";
            }
        });

        // Endpoint to list all bookings
        get("/list", (req, res) -> {
            List<Booking> bookings = BookingDAO.listAllBookings();

            res.type("application/json");
            res.status(200);  // OK
            return gson.toJson(bookings);  // Serialize the list of bookings to JSON
        });

        // Cancel all
        delete("/cancel_all", (req, res) -> {
            BookingDAO.cancelAllBookings();
            res.status(200);  // OK
            return "All bookings canceled successfully!";
        });
    }
}
