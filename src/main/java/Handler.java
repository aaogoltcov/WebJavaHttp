import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public abstract class Handler {
    final String method;
    final String url;

    public Handler(String method, String url) {
        this.method = method;
        this.url = url;
    }

    abstract void handle(HttpExchange exchange) throws IOException;
}
