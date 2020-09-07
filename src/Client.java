
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class Client {

    private class ClientWindow implements Runnable{

        private JFrame frame;

        private void setJoinUI() {
            frame = new JFrame("Join");
            frame.setSize(380, 300);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


            JPanel joinPanel = new JPanel();
            joinPanel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
            JLabel nameLabel = new JLabel("Name:");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 15));
            JTextField nameField = new JTextField(15);
            nameField.setFont(new Font("Arial", Font.PLAIN, 15));
            JButton joinButton = new JButton("Join");
            joinButton.setFont(new Font("Arial", Font.PLAIN, 15));

            joinPanel.add(nameLabel);
            joinPanel.add(nameField);
            joinPanel.add(joinButton);


            JPanel errorPanel = new JPanel();
            errorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));
            JLabel errorLabel = new JLabel("This name is already taken");
            errorLabel.setFont(new Font("Arial", Font.BOLD, 15));
            //errorPanel.setVisible(false);

            errorPanel.add(errorLabel);


            frame.getContentPane().add(BorderLayout.NORTH, joinPanel);
            frame.getContentPane().add(BorderLayout.SOUTH, errorPanel);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        private void setChatUI() {
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
            JTextArea messages = new JTextArea(20, 30);
            messages.setFont(new Font("Arial", Font.PLAIN, 20));

            JScrollPane scroll = new JScrollPane(messages);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            messagesPanel.add(scroll);


            JPanel sendPanel = new JPanel();
            sendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JTextArea input = new JTextArea(3, 30);
            input.setFont(new Font("Arial", Font.PLAIN, 16));

            JScrollPane inputScroll = new JScrollPane(input);
            inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            inputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            JButton sendButton = new JButton("Send");
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

        public ClientWindow() {
        }

        @Override
        public void run() {
            setJoinUI();
            // join

            setChatUI();
            // receive messages
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
    }

    private Client() {
        try {
            SwingUtilities.invokeAndWait(new ClientWindow());
        } catch (InterruptedException e) {
            System.out.println("Error");
        } catch (InvocationTargetException e) {
            System.out.println("Error");
        }
    }

}
