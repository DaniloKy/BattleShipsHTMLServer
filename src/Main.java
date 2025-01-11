import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final int PORT = 8000;
    private final static int MAX_CLIENTS = 2;
    public static Map<String, Player> playerClients = new HashMap<>();
    public static String gameState = "waitingForPlayers";
    private static ConcurrentHashMap<String, String> sharedState = new ConcurrentHashMap<>();
    private static String lastMessageSent;


    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new ConnectionHandler());
        server.createContext("/send", new SendHandler());
        server.createContext("/cancelCon", new CancellationHandler());
        //server.createContext("/receive", new ReceiveHandler());
        server.createContext("/stateRequest", new StateRequestHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000...");


    }

    static class SendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String clientId = exchange.getRequestHeaders().getFirst("Client-Username");
                String message = new String(exchange.getRequestBody().readAllBytes());

                sharedState.put(clientId, message);
                lastMessageSent = message;

                String response = "Message send!";
                System.out.println("MESSAGE send");
                System.out.println(message);
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class ReceiveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String clientId = exchange.getRequestHeaders().getFirst("Client-ID");
                String message = sharedState.getOrDefault(clientId, "No new messages");
                System.out.println("MESSAGE RECEIVED");
                System.out.println(message);
                System.out.println("LAST MESSAGE: "+lastMessageSent);
                exchange.sendResponseHeaders(200, lastMessageSent.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(lastMessageSent.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class StateRequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("STATUS REQUEST");
            if ("GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, gameState.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(gameState.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class ConnectionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (playerClients.size() < MAX_CLIENTS) {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String clientUsername = exchange.getRequestHeaders().getFirst("Client-Username");
                    System.out.println(clientUsername + " connected to the server!");
                    if (playerClients.size() <= 0){
                        System.out.println("PLAYER 1");
                        //client1 = new Player(clientUsername);
                        playerClients.put(clientUsername, new Player(clientUsername));
                        gameState = "waitingForLastPlayer";
                    } else {
                        System.out.println("PLAYER 2");
                        playerClients.put(clientUsername, new Player(clientUsername));
                        gameState = "canCreateBoards";
                    }
                    exchange.sendResponseHeaders(200, 10);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Connected!".getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            } else {
                System.out.println("MAX PLAYER");
                exchange.sendResponseHeaders(303, 10);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write("max players, wait for your turn".getBytes());
                }
                return;
            }

        }
    }

    static class CancellationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if (!playerClients.isEmpty()) {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String clientUsername = exchange.getRequestHeaders().getFirst("Client-Username");
                    System.out.println(clientUsername+ " canceled his connection");
                    playerClients.remove(clientUsername);
                    exchange.sendResponseHeaders(200, 10);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Canceled!".getBytes());
                    }
                }
            } else {
                System.out.println("No players");
                exchange.sendResponseHeaders(303, 10);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write("no players, wait for your turn".getBytes());
                }
            }
        }
    }

}