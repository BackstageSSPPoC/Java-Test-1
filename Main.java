import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Route: /
        server.createContext("/", exchange -> {
            String response = "{\"status\":\"UP\", \"message\":\"Hello from Java!\"}";
            sendResponse(exchange, response);
        });

        // Route: /hello
        server.createContext("/hello", exchange -> {
            String response = "{\"message\":\"Hello World from Java App!\"}";
            sendResponse(exchange, response);
        });

        server.start();
        System.out.println("Server running on port " + port);
    }

    server.createContext("/", exchange -> {
    String response = """
        <html>
        <body>
            <h1>Hello from Java!</h1>
            <p>Status: UP</p>
        </body>
        </html>
        """;
    exchange.getResponseHeaders().set("Content-Type", "text/html");  // ← html set karo
    exchange.sendResponseHeaders(200, response.length());
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
});
}
