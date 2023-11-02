import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/messages", "GET response");
        server.addHandler("POST", "/messages", "POST response");

        server.listen(9999);
    }
}
