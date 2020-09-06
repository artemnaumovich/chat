
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;

public class Server {
    private ArrayList<Socket> clientsSockets = new ArrayList<>();
    private HashSet<String> clientsNames = new HashSet<>();

    private class Connection implements Runnable {
        private Socket clientSocket;
        private String name;
        private BufferedReader in;
        private BufferedWriter out;

        public Connection(Socket socket) throws IOException {
            clientSocket = socket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            log("Client " + socket.getInetAddress() + ": " + socket.getPort() + " connected");
        }

        private void sendAll(String message) throws IOException {
            for (Socket client: clientsSockets) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                sendTo(writer, message);
            }
        }

        private void disconnect () throws IOException {
            clientSocket.close();
            clientsSockets.remove(clientSocket);
            clientsNames.remove(name);
        }

        private String receiveMessage() throws IOException {
            String lenStr = in.readLine();

            int len = Integer.parseInt(lenStr);
            String[] lines = new String[len];
            for (int i = 0; i < len; i++) {
                lines[i] = in.readLine();
            }

            return String.join("\n", lines);
        }

        private void sendTo(BufferedWriter writer, String message) throws IOException{
            String[] lines = message.split("\n");
            int len = lines.length;

            writer.write(len + "\r\n");
            writer.write(message + "\r\n");
            writer.flush();
        }

        private void showClient() {
            System.out.println("Clients: ");
            for (String cName: clientsNames) {
                System.out.println("->" + cName);
            }
        }

        @Override
        public void run() {
            do {
                try {
                    name = receiveMessage();
                    log("Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " try to join with name " + name);
                    if (clientsNames.contains(name)) {
                        sendTo(out, "taken");
                    } else {
                        clientsSockets.add(clientSocket);
                        clientsNames.add(name);
                        sendTo(out, "joined");
                        sendAll(name + " joined to the chat");
                        log("Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " joined with name " + name);
                        showClient();
                        break;
                    }
                } catch (SocketException e) {
                    try {
                        disconnect();
                        log("Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " disconnected");
                    } catch (IOException ioe) {
                        log("Failed to disconnect client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                    }
                    return;
                } catch (IOException e) {
                    log("Failed to receive name from " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                }
            } while (clientSocket.isConnected());

            while (!clientSocket.isClosed()) {
                String message;
                try {
                    message = receiveMessage();
                } catch (SocketException e) {
                    try {
                        disconnect();
                        sendAll(name + " left the chat");
                        log("Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " disconnected");
                    } catch (IOException ioe) {
                        log("Failed to disconnect client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                    }
                    return;
                } catch (IOException e) {
                    log("Failed to receive message from " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                    continue;
                }

                try {
                    sendAll(message);
                } catch (IOException e) {
                    log("Failed to send message for all clients from " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                }
            }

        }
    }

    public static void main(String[] args) {
        Server server = new Server();
    }

    private void log(String message) {
        System.out.println(message);
    }

    private Server() {
        try (ServerSocket serverSocket = new ServerSocket(8686)) {
            System.out.println("Server is running...");
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    Thread thread = new Thread(new Connection(socket));
                    thread.start();
                } catch (IOException e) {
                    log("Connection failed");
                    throw new IOException();
                }
            }
        } catch (IOException e) {
            log("Unable to start server on port 8686");
        }
    }
}
