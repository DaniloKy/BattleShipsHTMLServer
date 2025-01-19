import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final int PORT = 8000;
    private final static int MAX_CLIENTS = 2;
    public static Map<String, Player> playerClients = new ConcurrentHashMap<>();
    public static String gameState = "waitingForPlayers";
    private static ConcurrentHashMap<String, String> sharedState = new ConcurrentHashMap<>();
    private static String lastMessageSent;
    private static final long timeoutInSeconds = 5;

    private static Player firstPlayer = null;


    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new ConnectionHandler());
        server.createContext("/send", new SendHandler());
        server.createContext("/cancelCon", new CancellationHandler());
        server.createContext("/stateRequest", new StateRequestHandler());
        server.createContext("/sendBoard", new SendBoardHandler());
        server.createContext("/sendAttack", new SendAttackHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000...");


        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    System.out.println("Check inactive players");
                    checkInactivePlayers();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static void checkInactivePlayers() {
        LocalDateTime now = LocalDateTime.now();
        if(!playerClients.isEmpty()){
            try {
                playerClients.forEach((playerName, player) -> {
                    System.out.println(playerName);
                    long secondsInactive = Duration.between(player.getLastActiveTime(), now).getSeconds();
                    if (secondsInactive > timeoutInSeconds) {
                        System.out.println("Player " + playerName + " timed out and was removed.");
                        playerClients.remove(playerName);
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
            }

        } else {
            System.out.println("no players yet, no check");
        }

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

    static class SendBoardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String clientUsername = exchange.getRequestHeaders().getFirst("Client-Username");
                String message = new String(exchange.getRequestBody().readAllBytes());
                System.out.println("MESSAGE RECEIVED");
                System.out.println(message);
                Player player = playerClients.get(clientUsername);
                player.setBoard(new Board(message));
                player.setPlayerBoardReady(true);
                System.out.println(player.getName()+", board: ");
                player.printBoard();
                if (firstPlayer == null) {
                    firstPlayer = player;
                    System.out.println(firstPlayer.getName() + " will start the game!");
                }
                boolean bothPlayerReady = true;
                for (Player player_: playerClients.values()) {
                    if (!player_.isPlayerBoardReady()) {
                        bothPlayerReady = false;
                        gameState = "waitingForLastPlayerToCreateBoard";
                        break;
                    }
                }
                if (bothPlayerReady)
                    gameState = "turn-" + firstPlayer;;
                exchange.sendResponseHeaders(200, gameState.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(gameState.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class SendAttackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String clientUsername = exchange.getRequestHeaders().getFirst("Client-Username");
                String message = new String(exchange.getRequestBody().readAllBytes());
                System.out.println("MESSAGE RECEIVED");
                System.out.println(message);
                message.trim();
                String[] coordS = message.split(",");

                if (coordS.length != 2) {
                    sendResponse(exchange, 400, "Invalid coordinate format"); // Bad Request
                    return;
                }

                try {
                    int y = Integer.parseInt(coordS[0].trim());
                    int x = Integer.parseInt(coordS[1].trim());
                    Coordinate attackCoordinate = new Coordinate(x, y);

                    if (playerClients.containsKey(clientUsername)){
                        Player playerAttacking = playerClients.get(clientUsername);
                        Player opponent = playerClients.values()
                                .stream()
                                .filter(player -> !player.getName().equals(clientUsername))
                                .findFirst()
                                .orElse(null);
                        System.out.println(playerAttacking.getName());
                        System.out.println(opponent.getName());
                        if (opponent != null){
                            boolean hit = playerAttacking.fireAndAttackOpp(opponent, attackCoordinate);
                            if (hit) {
                                System.out.println(playerAttacking.getName()+" hit "+opponent.getName());
                                if (checkIfPlayerHasWon(opponent)) {
                                    gameState = "endGame";
                                    System.out.println("Player " + playerAttacking.getName() + " has won the game!");
                                }/* else {
                                    opponent.printBoard();
                                    gameState = "turn-"+opponent.getName();
                                    System.out.println("Player " + opponent.getName() + " turn");
                                }*/
                            }else {
                                System.out.println(playerAttacking.getName()+" miss");
                                gameState = "turn-"+opponent.getName();
                                System.out.println("Player " + opponent.getName() + " turn");
                            }
                            sendResponse(exchange, 200, gameState);
                            return;
                        } else {
                            System.out.println("Opponent not found");
                            sendResponse(exchange, 404, "Opponent not found");
                            return;
                        }
                    } else {
                        System.out.println("Player not found");
                        sendResponse(exchange, 404, "Player not found");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid coordinates: " + message);
                    sendResponse(exchange, 400, "Invalid coordinate format");
                    return;
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }

        private boolean checkIfPlayerHasWon(Player opponent) {
            return opponent.getShips().values().stream().allMatch(ship -> ship.isShipDestroyed());
        }
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class ReceiveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String clientId = exchange.getRequestHeaders().getFirst("Client-Username");
                //String message = sharedState.getOrDefault(clientId, "No new messages");
                String message = new String(exchange.getRequestBody().readAllBytes());

                System.out.println("MESSAGE RECEIVED");
                System.out.println(message);
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
            //System.out.println("STATUS REQUEST");
            if ("GET".equals(exchange.getRequestMethod())) {
                String clientId = exchange.getRequestHeaders().getFirst("Client-Username");
                playerClients.get(clientId).updateLastTimeRequest();
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
                    if (playerClients.size() <= 0) {
                        System.out.println("PLAYER 1");
                        playerClients.put(clientUsername, new Player(clientUsername));
                        playerClients.get(clientUsername).updateLastTimeRequest();
                        gameState = "waitingForLastPlayer";
                    } else {
                        System.out.println("PLAYER 2");
                        playerClients.put(clientUsername, new Player(clientUsername));
                        playerClients.get(clientUsername).updateLastTimeRequest();
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