# Battleships Multiplayer Server

This repository contains the **server-side** code for a multiplayer Battleships game, originally created as a school project. The client-side component, which includes a Graphical User Interface (GUI) written in Java, connects and communicates with this server via HTTP requests to enable 2-player multiplayer gameplay.

## 🚀 Overview

The server acts as the central hub managing the game state, player connections, board tracking, and attack coordination. It runs a simple built-in Java HTTP server (`com.sun.net.httpserver.HttpServer`) on **port 8000** and maintains the logic to keep two clients in sync.

## 📡 Endpoints

The server exposes several HTTP endpoints for the clients:

- `GET /` - Connects a player to the server. If max players are reached, responds with instructions to wait.
- `GET /cancelCon` - Disconnects the player and removes them from the server.
- `GET /stateRequest` - Polls the current state of the game (e.g., `waitingForLastPlayer`, `canCreateBoards`, `playing-[response_json]`).
- `POST /sendBoard` - Receives a player's board layout as a string, initializes their `Board` object, and marks them as ready.
- `POST /sendAttack` - Receives attack coordinates (`y, x`). Checks against the opponent's board and returns an `AttackResponse` including whether it was a hit and whose turn is next.
- `POST /send` - Allows generic message sending between clients (chat functionality).

## 🛠️ Built With

- **Java** (specifically `com.sun.net.httpserver` for the core server architecture)
- **Jackson** (`com.fasterxml.jackson.databind.ObjectMapper`) for serializing and deserializing JSON responses (like `AttackResponse`).

## 🏃 Getting Started

1. Ensure you have the Java JDK installed.
2. Compile and run the `Main.java` class within the `src` directory.
3. The server will start on `http://localhost:8000`.
4. Run the Battleships Client (from the related client repository) on two separate instances to connect and play!
