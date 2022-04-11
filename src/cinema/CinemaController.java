package cinema;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class CinemaController {
    Cinema cinema = new Cinema(9, 9);
    Map<String, Seat> purchases = new ConcurrentHashMap<>();

    @GetMapping("/seats")
    public Cinema getSeats() {
        return cinema;
    }

    @PostMapping(value = "/purchase")
    public synchronized ResponseEntity<Object> ticketPurchase(@RequestBody Seat jsonData) {

        Seat seat = new Seat(jsonData.getRow(), jsonData.getColumn());

        if (cinema.getAvailable_seats().remove(seat)) {
            Purchase purchase = new Purchase(seat);
            purchases.put(purchase.getToken(), seat);
            return ResponseEntity.ok(purchase);
        } else if (seat.getColumn() > cinema.getTotal_columns() ||
                seat.getColumn() < 1 ||
                seat.getRow() > cinema.getTotal_rows() ||
                seat.getRow() < 1) {
            return ResponseEntity.badRequest().body(
                    new ConcurrentHashMap<>(Map.of("error", "The number of a row or a column is out of bounds!")));
        } else {
            return ResponseEntity.badRequest().body(
                    new ConcurrentHashMap<>(Map.of("error", "The ticket has been already purchased!"))
            );
        }
    }

    @PostMapping(value = "/return")
    public synchronized ResponseEntity<Object> ticketReturn(@RequestBody Map<String, String> tokenMap) {
        String token = tokenMap.get("token");

        if (purchases.containsKey(token)) {
            Seat seat = purchases.get(token);
            purchases.remove(token, seat);
            cinema.getAvailable_seats().add(seat);
            return ResponseEntity.ok().body(new ConcurrentHashMap<>(Map.of("returned_ticket", seat)));
        } else {
            return ResponseEntity.badRequest().body(new ConcurrentHashMap<>(Map.of("error", "Wrong token!")));
        }
    }

    @PostMapping(value = "/stats")
    public synchronized ResponseEntity<Object> showStats(@RequestParam Map<String, String> passwordMap) {
        int isPasswordCorrect = passwordMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals("password"))
                .filter(entry -> entry.getValue().equals("super_secret"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()).size();

        if (isPasswordCorrect == 1) {
            return ResponseEntity.ok().body(new ConcurrentHashMap<>(Map.of(
                    "current_income", currentIncome(),
                    "number_of_available_seats", cinema.getAvailable_seats().size(),
                    "number_of_purchased_tickets", purchases.size())));
        } else {
            return new ResponseEntity<>(
                    new ConcurrentHashMap<String, String>(Map.of("error", "The password is wrong!")),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    private synchronized int currentIncome() {
        int result = 0;
        for (var entry : purchases.entrySet()) {
            result += entry.getValue().getPrice();
        }
        return result;
    }
}