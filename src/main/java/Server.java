import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {
    Map<String, List<Handler>> handlersMap = new HashMap<>();

    public void listen(int port) {
        if (!handlersMap.isEmpty()) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

                handlersMap.forEach((url, handlers) -> server.createContext(url, (exchange -> {
                    if (handlers.stream().anyMatch(handler -> handler.method.equals(exchange.getRequestMethod()))) {
                        handlers.forEach(handler -> {
                            if (handler.method.equals(exchange.getRequestMethod())) {
                                try {
                                    exchange.sendResponseHeaders(200, handler.responseText.getBytes().length);
                                    OutputStream output = exchange.getResponseBody();
                                    output.write(handler.responseText.getBytes());
                                    output.flush();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                        exchange.close();
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }
                })));

                server.setExecutor(null);
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addHandler(String method, String url, String responseText) {
        Handler handler = new Handler(method, url, responseText);

        handlersMap.putIfAbsent(url, new ArrayList<>());
        handlersMap.get(url).add(handler);
    }
}
