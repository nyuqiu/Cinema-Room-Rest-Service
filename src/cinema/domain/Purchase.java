package cinema.domain;

import java.util.UUID;

public class Purchase {
    private String token;
    private Seat ticket;

    public Purchase(Seat seat) {
        this.ticket = seat;
        this.token = UUID.randomUUID().toString();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Seat getTicket() {
        return ticket;
    }

    public void setTicket(Seat ticket) {
        this.ticket = ticket;
    }
}
