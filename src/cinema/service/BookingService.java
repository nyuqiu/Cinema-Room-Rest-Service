package cinema.service;

import cinema.domain.Cinema;
import cinema.domain.Purchase;
import cinema.domain.Seat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BookingService {
    Cinema cinema = new Cinema(9, 9);
    Map<String, Seat> purchases = new ConcurrentHashMap<>();

    public synchronized ResponseEntity<Object> purchaseTicket(Seat jsonSeat){

        Seat seat = new Seat(jsonSeat.getRow(), jsonSeat.getColumn());

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

    public synchronized ResponseEntity<Object> ticketReturn(Map<String, String> tokenMap) {
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

    public synchronized ResponseEntity<Object> showStats(Map<String, String> passwordMap) {
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
