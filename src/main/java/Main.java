import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

public class Main {
    private static final List<Field> data = new ArrayList<>();

    public static void main(String[] args) {
        final var server = new Server();

        data.add(new Field("10001", "first", "description"));
        data.add(new Field("10002", "second", "description"));
        data.add(new Field("10003", "third", "description"));

        server.addHandler(new Handler("GET", "/messages") {
            @Override void handle(HttpExchange exchange) throws IOException {
                URI uri = exchange.getRequestURI();
                Map<String, String> queryParams = server.getQueryParams(uri);
                OutputStream output = exchange.getResponseBody();

                List<Field> response = data.stream()
                        .filter(item -> {
                            String id = queryParams.get("id");

                            return item.id.equals(id);
                        })
                        .toList();

                String json = new Gson().toJson(response);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.getBytes().length);
                output.write(json.getBytes());
                output.flush();
            }
        });
        server.addHandler(new Handler("POST", "/messages") {
            @Override void handle(HttpExchange exchange) throws IOException {
                String contentType = exchange.getRequestHeaders().get("Content-Type").get(0);
                OutputStream output = exchange.getResponseBody();

                if (!contentType.equals("application/x-www-form-urlencoded")) {
                    exchange.sendResponseHeaders(400, 0);
                    output.write("Bad request, please provide application/x-www-form-urlencoded content type.".getBytes());
                    output.flush();

                    return;
                }

                Map<String, String> postParams = server.getPostParams(exchange.getRequestBody());
                Gson gson = new Gson();
                String postParamsJson = gson.toJson(postParams);
                Field request = gson.fromJson(postParamsJson, Field.class);

                if (request != null) {
                    data.add(request);
                }

                String json = gson.toJson(data);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.getBytes().length);

                output.write(json.getBytes());
                output.flush();
            }
        });

        server.listen(9999);
    }
}
