
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

    private void log(String type, String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String timeStamp = dateFormat.format(date);
        System.out.println(timeStamp + " | " + type + " | " + message);
    }

    private class ClientWindow{

        private JFrame frame;
        private JTextField nameInput;
        private JPanel errorPanel;
        private JTextArea messages;
        private JTextArea messageInput;

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

            frame.dispose();

        }

        private class setJoinUI implements Runnable {

            private String TITLE = "Join";
            private int WIDTH = 380;
            private int HEIGHT = 300;
            private Font FONT = new Font("Arial", Font.PLAIN, 15);

            @Override
            public void run() {
                frame = new JFrame(TITLE);
                frame.setSize(WIDTH, HEIGHT);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                JPanel joinPanel = new JPanel();
                errorPanel = new JPanel();

                JLabel nameLabel = new JLabel("Name:");
                nameInput = new JTextField(15);
                JButton joinButton = new JButton("Join");

                JLabel errorLabel = new JLabel("This name is already taken");

                joinPanel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
                errorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));

                nameLabel.setFont(FONT);
                nameInput.setFont(FONT);
                joinButton.setFont(FONT);
                errorLabel.setFont(FONT);

                joinButton.addActionListener(new ButtonListener());

                joinPanel.add(nameLabel);
                joinPanel.add(nameInput);
                joinPanel.add(joinButton);
                errorPanel.add(errorLabel);

                errorPanel.setVisible(false);

                nameInput.setFocusable(true);

                frame.getContentPane().add(BorderLayout.NORTH, joinPanel);
                frame.getContentPane().add(BorderLayout.SOUTH, errorPanel);

                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }


        }

        private class setChatUI implements Runnable {

            private String TITLE = "Chat";
            private int WIDTH = 700;
            private int HEIGHT = 700;
            private String CHAT_NAME = "GLOBAL CHAT";
            private int MESSAGES_ROWS = 20;
            private int MESSAGES_COLUMNS = 30;
            private int INPUT_ROWS = 3;
            private int INPUT_COLUMNS = 30;

            private Font CHAT_NAME_FONT = new Font("Arial", Font.BOLD, 30);
            private Font MESSAGES_FONT = new Font("Arial", Font.PLAIN, 20);
            private Font INPUT_FONT = new Font("Arial", Font.PLAIN, 16);
            private Font BUTTON_FONT = new Font("Arial", Font.BOLD, 16);

            @Override
            public void run() {
                frame.dispose();
                frame = new JFrame(TITLE);
                frame.setSize(WIDTH, HEIGHT);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel headerPanel = new JPanel();
                JLabel nameLabel = new JLabel(CHAT_NAME);

                JPanel messagesPanel = new JPanel();
                messages = new JTextArea(MESSAGES_ROWS, MESSAGES_COLUMNS);
                JScrollPane scroll = new JScrollPane(messages);

                JPanel sendPanel = new JPanel();
                messageInput = new JTextArea(INPUT_ROWS, INPUT_COLUMNS);
                JScrollPane inputScroll = new JScrollPane(messageInput);
                JButton sendButton = new JButton("Send");

                nameLabel.setFont(CHAT_NAME_FONT);
                messages.setFont(MESSAGES_FONT);
                messageInput.setFont(INPUT_FONT);
                sendButton.setFont(BUTTON_FONT);

                messagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                sendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

                messages.setEditable(false);
                messages.setLineWrap(true);
                messageInput.setLineWrap(true);

                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                inputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                sendButton.addActionListener(new ButtonListener());

                sendButton.setBackground(Color.CYAN);
                headerPanel.setBackground(Color.LIGHT_GRAY);
                sendPanel.setBackground(Color.LIGHT_GRAY);

                headerPanel.add(nameLabel);
                messagesPanel.add(scroll);
                sendPanel.add(inputScroll);
                sendPanel.add(sendButton);


                frame.getContentPane().add(BorderLayout.NORTH, headerPanel);
                frame.getContentPane().add(BorderLayout.CENTER, messagesPanel);
                frame.getContentPane().add(BorderLayout.SOUTH, sendPanel);

                frame.setLocationRelativeTo(null);

                frame.setVisible(true);
            }
        }

        private class ButtonListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent event) {
                String command = event.getActionCommand();
                String message;

                if (command.equals("Join")) {
                    message = nameInput.getText();
                } else if (command.equals("Send")) {
                    message = messageInput.getText();
                    messageInput.setText(null);
                } else {
                    log("ERROR", "Unexpected command");
                    return;
                }

                try {
                    send(message);
                } catch (IOException e) {
                    log("ERROR", "Failed to send message to server");
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
    }

}
