/*
    학번 : 2091193
    이름 : 최재영
 */


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CalcClientGUI {

    private final JFrame frame;
    private final String serverAddress;
    private final int serverPort;

    private JButton calcButton;

    private ObjectOutputStream out;
    private DataInputStream in;

    public CalcClientGUI(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        frame = new JFrame("CalcClient GUI");

        buildGUI();

        frame.setSize(400, 300);
        frame.setLocation(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 51111;

        new CalcClientGUI(serverAddress, serverPort);
    }

    /*
     * GUI related methods
     */
    private void buildGUI() {
        frame.add(createInputPanel(), BorderLayout.NORTH);
        frame.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();

        JTextField t_op1 = new JTextField(5);
        JTextField t_op2 = new JTextField(5);
        JLabel equal = new JLabel("=");

        String[] operators = {"+", "-", "*", "/"};
        JComboBox<String> t_operator = new JComboBox<>(operators);

        JTextField t_result = new JTextField(5);
        t_result.setEditable(false);

        calcButton = new JButton("계산");
        calcButton.setEnabled(false);

        panel.add(t_op1);
        panel.add(t_operator);
        panel.add(t_op2);
        panel.add(equal);
        panel.add(t_result);
        panel.add(calcButton);

        // Event Listeners
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String op1Text = t_op1.getText();
                String operatorText = t_operator.getSelectedItem().toString();
                String op2Text = t_op2.getText();

                if (op1Text.isEmpty() || operatorText.isEmpty() || op2Text.isEmpty()) {
                    return;
                }

                double op1;
                try {
                    op1 = Double.parseDouble(op1Text);
                } catch (NumberFormatException ex) {
                    t_op1.setText("");
                    return;
                }

                double op2;
                try {
                    op2 = Double.parseDouble(op2Text);
                } catch (NumberFormatException ex) {
                    t_op2.setText("");
                    return;
                }

                char operator = operatorText.charAt(0);

                CalcExpr msg = new CalcExpr(op1, operator, op2);
                sendMessage(msg);

                double result = receiveMessage();
                t_result.setText(String.format("%.2f", result));
            }
        };
        calcButton.addActionListener(listener);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3));

        JButton connectButton = new JButton("접속하기");
        JButton disconnectButton = new JButton("접속 끊기");
        disconnectButton.setEnabled(false);
        JButton exitButton = new JButton("종료하기");

        panel.add(connectButton);
        panel.add(disconnectButton);
        panel.add(exitButton);

        // Event Listeners
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connectToServer();
                } catch (IOException ex) {
                    System.err.println("클라이언트 접속 오류: " + ex.getMessage());
                    return;
                }

                calcButton.setEnabled(true);
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                exitButton.setEnabled(false);
            }
        });
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    disconnect();
                } catch (IOException ex) {
                    System.err.println("클라이언트 닫기 오류: " + ex.getMessage());
                    return;
                }

                calcButton.setEnabled(false);
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                exitButton.setEnabled(true);
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        return panel;
    }

    /*
     * socket related methods
     */
    private void sendMessage(CalcExpr msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("메시지 전송 오류: " + e.getMessage());
        }
    }

    private double receiveMessage() {
        double result = 0;
        try {
            result = in.readDouble();
        } catch (IOException e) {
            System.err.println("메시지 수신 오류: " + e.getMessage());
        }
        return result;
    }

    private void connectToServer() throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();

        out = new ObjectOutputStream(new BufferedOutputStream(os));
        in = new DataInputStream(new BufferedInputStream(is));
    }

    private void disconnect() throws IOException {
        sendMessage(null);
        out.close();
        in.close();
    }
}