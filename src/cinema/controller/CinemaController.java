package cinema.controller;

import cinema.domain.Cinema;
import cinema.domain.Seat;
import cinema.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RestController
public class CinemaController {

    private BookingService bookingService;
    Cinema cinema = new Cinema(9, 9);
    Map<String, Seat> purchases = new ConcurrentHashMap<>();

    @GetMapping("/seats")
    public Cinema getSeats() {
        return cinema;
    }

    @PostMapping(value = "/purchase")
    public synchronized ResponseEntity<Object> ticketPurchase(@RequestBody Seat jsonData) {
        return bookingService.purchaseTicket(jsonData);
    }

    @PostMapping(value = "/return")
    public synchronized ResponseEntity<Object> ticketReturn(@RequestBody Map<String, String> tokenMap) {
        return bookingService.ticketReturn(tokenMap);
    }

    @PostMapping(value = "/stats")
    public synchronized ResponseEntity<Object> showStats(@RequestParam Map<String, String> passwordMap) {
        return bookingService.showStats(passwordMap);
    }
}