//1971068 전지훈

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class CalcClientGUI {
    private JFrame frame;
    private String serverAddress = "localhost";  // 서버 주소 설정
    private int serverPort = 54321;  // 서버 포트 설정
    private Socket socket;

    private JButton b_send;

    private ObjectOutputStream out;
    private DataInputStream in;

    public CalcClientGUI() {
        frame = new JFrame("CalcClientGUI");

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;


        buildGUI();

        frame.setSize(400, 400);  // 화면 크기 조정
        frame.setLocation(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    private void buildGUI() {
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(createInputPanel(), BorderLayout.NORTH);

        frame.getContentPane().add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JTextField t_input1 = new JTextField(4);
        JTextField t_calc = new JTextField(1);    // 연산자
        JTextField t_input2 = new JTextField(4);
        JLabel equal = new JLabel("=");

        JTextField t_result = new JTextField(5);
        t_result.setEditable(false);

        b_send = new JButton("계산");
        b_send.setEnabled(false);

        panel.add(t_input1);
        panel.add(t_calc);
        panel.add(t_input2);
        panel.add(equal);
        panel.add(t_result);
        panel.add(b_send);

        ActionListener calculateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String op1Text = t_input1.getText();
                String operatorText = t_calc.getText();  // 연산자를 입력받고
                String op2Text = t_input2.getText();

                if (op1Text.isEmpty() || operatorText.isEmpty() || op2Text.isEmpty()) {
                    return;
                }

                double op1;
                try {
                    op1 = Double.parseDouble(op1Text);
                } catch (NumberFormatException ex) {
                    t_input1.setText("");
                    return;
                }

                double op2;
                try {
                    op2 = Double.parseDouble(op2Text);
                } catch (NumberFormatException ex) {
                    t_input2.setText("");
                    return;
                }

                // 연산자 확인 및 계산 요청 전송
                char operator = operatorText.charAt(0);

                if (operator == '+' || operator == '-' || operator == '*' || operator == '/') {
                    CalcExpr msg = new CalcExpr(op1, operator, op2);
                    sendMessage(msg);

                    double result = receiveMessage();
                    t_result.setText(String.format("%.2f", result));
                } else {
                    t_calc.setText("");  // 잘못된 연산자 입력 시 초기화
                }
            }
        };
        //엔터 전송
        t_input1.addActionListener(calculateListener);
        t_input2.addActionListener(calculateListener);
        t_calc.addActionListener(calculateListener);

        b_send.addActionListener(calculateListener);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3));

        JButton connectButton = new JButton("접속하기");
        JButton disconnectButton = new JButton("접속 끊기");
        JButton exitButton = new JButton("종료하기");

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connectToServer();
                } catch (IOException ex) {
                    System.err.println("클라이언트 접속 오류: " + ex.getMessage());
                    return;
                }

                b_send.setEnabled(true);
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                exitButton.setEnabled(false);
            }
        });
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();

                b_send.setEnabled(false);
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
        panel.add(connectButton);
        panel.add(disconnectButton);
        panel.add(exitButton);
        disconnectButton.setEnabled(false);

        return panel;
    }


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
        socket = new Socket(serverAddress, serverPort);
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();

        out = new ObjectOutputStream(new BufferedOutputStream(os));
        in = new DataInputStream(new BufferedInputStream(is));
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                out.close();
                socket.close();
                System.out.println("서버와의 연결을 끊었습니다.");
            }
        } catch (IOException e) {
            System.err.println("연결 종료 오류: " + e.getMessage());
        }
    }
    public static void main(String[] args) {

        new CalcClientGUI();
    }

}
