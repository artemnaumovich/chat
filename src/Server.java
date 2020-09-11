
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class Server {
    private ArrayList<Socket> clientsSockets = new ArrayList<>();
    private HashSet<String> clientsNames = new HashSet<>();

    private class Connection implements Runnable {
        private Socket clientSocket;
        private String name;
        private BufferedReader in;
        private BufferedWriter out;
        private boolean joined;

        public Connection(Socket socket) throws IOException {
            clientSocket = socket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            joined = false;
            log("INFO", "Client " + socket.getInetAddress() + ": " + socket.getPort() + " connected");
        }

        private String formatInfoMessage(String message) {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            String timeStamp = dateFormat.format(date);
            return "<" + timeStamp + "> : " + message;
        }

        private String formatPublicMessage(String message) {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            String timeStamp = dateFormat.format(date);
            return "<" + timeStamp + " " + name + "> : " + message;
        }

        private void sendInfoMessage(String message) throws IOException{
            String formattedMessage = formatInfoMessage(message);
            sendAll(formattedMessage);
        }

        private void sendPublicMessage(String message) throws IOException {
            String formattedMessage = formatPublicMessage(message);
            sendAll(formattedMessage);
        }

        private void sendAll(String message) throws IOException {
            for (Socket client: clientsSockets) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                sendTo(writer, message);
            }
        }

        private void disconnect() throws IOException {
            clientSocket.close();
            if (joined) {
                clientsSockets.remove(clientSocket);
                clientsNames.remove(name);
            }
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
                    log("INFO","Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " try to join with name " + name);
                    if (clientsNames.contains(name)) {
                        sendTo(out, "taken");
                    } else {
                        clientsSockets.add(clientSocket);
                        clientsNames.add(name);
                        joined = true;
                        sendTo(out, "joined");
                        sendInfoMessage(name + " joined to the chat");
                        log("INFO","Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " joined with name " + name);
                        showClient();
                        break;
                    }
                } catch (SocketException e) {
                    try {
                        disconnect();
                        log("INFO","Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " disconnected");
                    } catch (IOException ioe) {
                        log("ERROR","Failed to disconnect client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                    }
                    return;
                } catch (IOException e) {
                    log("ERROR","Failed to receive name from " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                }
            } while (clientSocket.isConnected());

            while (!clientSocket.isClosed()) {
                String message;
                try {
                    message = receiveMessage();
                } catch (SocketException e) {
                    try {
                        disconnect();
                        sendInfoMessage(name + " left the chat");
                        log("INFO","Client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort() + " disconnected");
                    } catch (IOException ioe) {
                        log("ERROR","Failed to disconnect client " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                    }
                    return;
                } catch (IOException e) {
                    log("ERROR","Failed to receive message from " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                    continue;
                }

                try {
                    sendPublicMessage(message);
                } catch (IOException e) {
                    log("ERROR","Failed to send message for all clients from " + clientSocket.getInetAddress() + ": " + clientSocket.getPort());
                }
            }

        }
    }

    public static void main(String[] args) {
        Server server = new Server();
    }

    private void log(String type, String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String timeStamp = dateFormat.format(date);
        System.out.println(timeStamp + " | " + type + " | " + message);
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
                    log("ERROR","Connection failed");
                    throw new IOException();
                }
            }
        } catch (IOException e) {
            log("ERROR","Unable to start server on port 8686");
        }
    }
}
