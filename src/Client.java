
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {

    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;

    private String receiveMessage() throws IOException {

        String lenStr = in.readLine();

        int len = Integer.parseInt(lenStr);
        String[] lines = new String[len];
        for (int i = 0; i < len; i++) {
            lines[i] = in.readLine();
        }

        return String.join("\n", lines);
    }

    private void send(String message) throws IOException{
        String[] lines = message.split("\n");
        int len = lines.length;

        out.write(len + "\r\n");
        out.write(message + "\r\n");
        out.flush();
    }

    private class ClientWindow{

        private JFrame frame;
        private JTextField nameInput;
        private JPanel errorPanel;
        private JTextArea messages;
        private JTextArea messageInput;

        private class setJoinUI implements Runnable {

            @Override
            public void run() {
                frame = new JFrame("Join");
                frame.setSize(380, 300);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


                JPanel joinPanel = new JPanel();
                joinPanel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
                JLabel nameLabel = new JLabel("Name:");
                nameLabel.setFont(new Font("Arial", Font.PLAIN, 15));
                nameInput = new JTextField(15);
                nameInput.setFont(new Font("Arial", Font.PLAIN, 15));
                JButton joinButton = new JButton("Join");
                joinButton.addActionListener(new ButtonListener());
                joinButton.setFont(new Font("Arial", Font.PLAIN, 15));

                joinPanel.add(nameLabel);
                joinPanel.add(nameInput);
                joinPanel.add(joinButton);


                errorPanel = new JPanel();
                errorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));
                JLabel errorLabel = new JLabel("This name is already taken");
                errorLabel.setFont(new Font("Arial", Font.BOLD, 15));
                errorPanel.setVisible(false);

                errorPanel.add(errorLabel);


                frame.getContentPane().add(BorderLayout.NORTH, joinPanel);
                frame.getContentPane().add(BorderLayout.SOUTH, errorPanel);

                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }


        }


        private class setChatUI implements Runnable {

            @Override
            public void run() {
                frame.dispose();
                frame = new JFrame("Chat");
                frame.setSize(700, 700);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel headerPanel = new JPanel();
                JLabel nameLabel = new JLabel("Global Chat");
                nameLabel.setFont(new Font("Arial", Font.BOLD, 30));
                headerPanel.setBackground(Color.LIGHT_GRAY);

                headerPanel.add(nameLabel);


                JPanel messagesPanel = new JPanel();
                messagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                messages = new JTextArea(20, 30);
                messages.setLineWrap(true);
                messages.setFont(new Font("Arial", Font.PLAIN, 20));

                JScrollPane scroll = new JScrollPane(messages);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                messagesPanel.add(scroll);


                JPanel sendPanel = new JPanel();
                sendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                messageInput = new JTextArea(3, 30);
                messageInput.setLineWrap(true);
                messageInput.setFont(new Font("Arial", Font.PLAIN, 16));

                JScrollPane inputScroll = new JScrollPane(messageInput);
                inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                inputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                JButton sendButton = new JButton("Send");
                sendButton.addActionListener(new ButtonListener());
                sendButton.setBackground(Color.CYAN);

                sendPanel.add(inputScroll);
                sendPanel.add(sendButton);
                sendPanel.setBackground(Color.LIGHT_GRAY);


                frame.getContentPane().add(BorderLayout.NORTH, headerPanel);
                frame.getContentPane().add(BorderLayout.CENTER, messagesPanel);
                frame.getContentPane().add(BorderLayout.SOUTH, sendPanel);

                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            }
        }


        public ClientWindow() {
            SwingUtilities.invokeLater(new setJoinUI());

            while (!clientSocket.isClosed()) {
                try {
                    String response = receiveMessage();
                    if (response.equals("joined")) {
                        log("INFO", "Joined to the chat");
                        break;
                    } else {
                        errorPanel.setVisible(true);
                    }
                } catch (IOException e) {
                    log("ERROR", "Failed to receive message to server");
                    try {
                        clientSocket.close();
                        log("INFO", "Disconnected");
                    } catch (IOException ioe) {
                        log("ERROR", "Failed to disconnect");
                        return;
                    }
                }
            }

            SwingUtilities.invokeLater(new setChatUI());

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log("ERROR", "Unexpected error");
                return;
            }

            while (!clientSocket.isClosed()) {
                try {
                    String message = receiveMessage();
                    messages.append(message + "\n");
                    messages.setCaretPosition(messages.getDocument().getLength());
                } catch (IOException e) {
                    log("ERROR", "Failed to receive message to server");
                    try {
                        clientSocket.close();
                        log("INFO", "Disconnected");
                    } catch (IOException ioe) {
                        log("ERROR", "Failed to disconnect");
                        return;
                    }
                }
            }

        }

        private class ButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent event) {
                String command = event.getActionCommand();
                if (command.equals("Join")) {
                    try {
                        send(nameInput.getText());
                    } catch (IOException e) {
                        log("ERROR", "Failed to send message to server");
                    }
                } else if (command.equals("Send")) {
                    try {
                        send(messageInput.getText());
                        messageInput.setText(null);
                    } catch (IOException e) {
                        log("ERROR", "Failed to send message to server");
                    }
                } else {
                    log("ERROR", "Unexpected command");
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
    }

    private void log(String type, String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String timeStamp = dateFormat.format(date);
        System.out.println(timeStamp + " | " + type + " | " + message);
    }

    private Client() {
        try {
            clientSocket = new Socket("127.0.0.1", 8686);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            log("ERROR","Failed to connect to server 127.0.0.1:8686");
            return;
        }

        log("INFO", "Connect to server 127.0.0.1:8686");

        new ClientWindow();
    }

}
