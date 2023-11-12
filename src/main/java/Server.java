import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;

public class Server {
    private static final String AND_DELIMITER = "&";
    private static final String EQUAL_DELIMITER = "=";
    private static final int PARAM_NAME_IDX = 0;
    private static final int PARAM_VALUE_IDX = 1;
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
                                    handler.handle(exchange);
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

    public void addHandler(Handler handler) {
        handlersMap.putIfAbsent(handler.url, new ArrayList<>());
        handlersMap.get(handler.url).add(handler);
    }

    public Map<String, String> getQueryParams(URI uri) {
        Map<String, String> queryParamsMap = new HashMap<>();

        String query = uri.getQuery();
        if (query != null) {
            String[] queryParams = query.split(AND_DELIMITER);
            parseRequestParams(queryParamsMap, queryParams);
        }

        return queryParamsMap;
    }

    public Map<String, String> getPostParams(InputStream inputStream) throws IOException {
        Map<String, String> postParamsMap = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;

        BufferedReader requestBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        while ((inputLine = requestBody.readLine()) != null) {
            stringBuilder.append(inputLine).append(" ");
        }
        requestBody.close();

        String[] postParams = stringBuilder.toString().split(AND_DELIMITER);
        parseRequestParams(postParamsMap, postParams);

        return postParamsMap;
    }

    private void parseRequestParams(Map<String, String> requestParamsMap, String[] requestParams) {
        for (String postParam : requestParams) {
            String[] param = postParam.split(EQUAL_DELIMITER);
            if (param.length > 0) {
                for (int i = 0; i < param.length; i++) {
                    requestParamsMap.put(param[PARAM_NAME_IDX], param[PARAM_VALUE_IDX]);
                }
            }
        }
    }
}
