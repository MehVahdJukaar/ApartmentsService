package bookings;

import bookings.Booking;
import bookings.BookingDAO;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static spark.Spark.*;

public class BookingApi {

    // Initialize the API
    public static void initialize() {
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
                res.status(201);  // Created
                return booking.id();
            } else {
                res.status(400);  // Bad Request
                return "Failed to add booking!";
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
            StringBuilder response = new StringBuilder();

            if (bookings.isEmpty()) {
                res.status(404);  // Not Found
                return "No bookings found!";
            }

            for (Booking booking : bookings) {
                response.append("Booking ID: ").append(booking.id())
                        .append(", Apartment ID: ").append(booking.apartmentID())
                        .append(", From: ").append(booking.fromDate())
                        .append(", To: ").append(booking.toDate())
                        .append(", Booked by: ").append(booking.who())
                        .append("\n");
            }

            res.type("text/plain");
            return response.toString();  // Return booking list as plain text
        });
    }
}
