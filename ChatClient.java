import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.plaf.metal.MetalBorders.TextFieldBorder;

public class ChatClient {
    private JTextArea output;
    private JTextField input;
    private JButton sendButton;
    private JButton quitButton;
    private JFrame frame;
    private JDialog aboutDialog;
    private JScrollPane scroll;
    private Socket skt;
    private InputStream is;
    private OutputStream os;
    private BufferedReader brin;
    private PrintStream ps;
    public static String username = "";
    private static int port;
    private static String host;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ChatClient() {
        output = new JTextArea(10, 30);
        Font oFont = new Font("TimesRoman", Font.BOLD, 18);
        output.setBorder(new TextFieldBorder());
        output.setFont(oFont);
        input = new JTextField(30);
        Font uFont = new Font("TimesRoman", Font.BOLD, 18);
        input.setFont(uFont);
        input.setForeground((Color.red));
        sendButton = new JButton("Send");
        quitButton = new JButton("Quit");
        output.setEditable(false);
        scroll = new JScrollPane(output);
        //scroll.setPreferredSize(new Dimension(30, 30));
    }

    public void launchFrame(int port, String host) throws IOException {
        frame = new JFrame("Chat Room");
        frame.setLayout(new BorderLayout());
        frame.add(output, BorderLayout.WEST);
        frame.add(input, BorderLayout.SOUTH);
        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(10, 1));
        p1.add(sendButton);
        p1.add(quitButton);
        p1.add(scroll);
        frame.add(scroll);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(p1, BorderLayout.CENTER);
        // Crear menús
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        file.add(quitMenuItem);
        mb.add(file);
        frame.setJMenuBar(mb);
        // Add Help menu to menu bar
        JMenu help = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new AboutHandler());
        help.add(aboutMenuItem);
        mb.add(help);
        // Attach listener to the appropriate components
        sendButton.addActionListener(new SendHandler());
        input.addActionListener(new SendHandler());
        frame.addWindowListener(new CloseHandler());
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        //Poner direccion ip en vez de xxx
        skt = new Socket(host, port);
        is = skt.getInputStream();
        brin = new BufferedReader(new InputStreamReader(is));
        os = skt.getOutputStream();
        ps = new PrintStream(os);
        RemoteReader r = new RemoteReader();
        out = new ObjectOutputStream(skt.getOutputStream());
        in = new ObjectInputStream(skt.getInputStream());
        out.flush();
        Thread t = new Thread(r);
        t.start();
        //frame.setSize(400,400);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private class SendHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = input.getText();
            //output.append(userNames.getSelectedItem() + ": " + text + "\n");
            ps.println(text);
            input.setText("");
        }
    }

    private class CloseHandler extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    private class AboutHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (aboutDialog == null) {
                aboutDialog = new AboutDialog(frame, "About", true);
            }
            //aboutDialog.setSize(300, 100);
            aboutDialog.pack();
            aboutDialog.setLocationRelativeTo(frame);
            aboutDialog.setVisible(true);
        }
    }

    private class AboutDialog extends JDialog implements ActionListener {
        public AboutDialog(Frame parent, String title, boolean modal) {
            super(parent, title, modal);
            String rollo = "\n Este chat permite enviar mensajes a los "
            + "\n demás usuarios del grupo a través de un servidor \n";
            output.append(rollo);
            JTextArea aboutTA = new JTextArea(5, 25);
            aboutTA.setEditable(false);
            Font uFont = new Font("TimesRoman", Font.BOLD, 18);
            aboutTA.setFont(uFont);
            aboutTA.setBackground((Color.LIGHT_GRAY));
            aboutTA.append(rollo);
            add(aboutTA, BorderLayout.CENTER);
            JPanel bPanel = new JPanel();
            JButton b = new JButton("OK");
            bPanel.add(b);
            add(bPanel, BorderLayout.SOUTH);
            b.addActionListener(this);
            pack();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }

    public static void main(String[] args) throws IOException {
        //username = JOptionPane.showInputDialog("Enter your username");
        host = JOptionPane.showInputDialog("Enter your hostname");
        port = Integer.parseInt(JOptionPane.showInputDialog("Enter port number"));

        ChatClient c = new ChatClient();
        c.launchFrame(port, host);
    }

    private class RemoteReader implements Runnable {
        public void run() {
            try {
                while (true) {
                    String linea = brin.readLine(); // lee una linea del Input Stream Reader 
                    output.append(linea + '\n');
                }
            } catch (Exception e) {
                System.out.println("Excepción en el readLine del Input Stream Reader");
            }
        }
    }
}



