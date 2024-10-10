import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class CalcClientGUI extends JFrame {
    private JTextArea t_display;
    private JTextField t_input;
    private JButton b_connect, b_disconnect, b_send, b_exit;
    private String serverAddress = "localhost";  // 서버 주소 설정
    private int serverPort = 54321;  // 서버 포트 설정
    private Socket socket;
    private Writer out;
    private Reader in;

    public CalcClientGUI() {
        super("ByteClientGUI");

        buildGUI();

        this.setBounds(800, 200, 400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        unconncetGUI(); //초기화를 위한 첨부터 실행될때부터 설정하기 위함

    }

    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);

        JPanel p_pink = new JPanel(new GridLayout(2, 0));
        p_pink.add(createInputPanel());
        p_pink.add(createControlPanel());
        add(p_pink, BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel p = new JPanel(new BorderLayout());

        t_display = new JTextArea();
        t_display.setEditable(false);

        p.add(new JScrollPane(t_display), BorderLayout.CENTER);

        return p;
    }


    private JPanel createInputAndControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createInputPanel());
        panel.add(createControlPanel());
        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        t_input = new JTextField(30);
        b_send = new JButton("보내기");

        b_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
                receiveMessage();
            }
        });
        t_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(); // 엔터 입력 메시지 전송
                receiveMessage();
            }
        });
        panel.add(t_input, BorderLayout.CENTER);
        panel.add(b_send, BorderLayout.EAST);
        t_input.setEnabled(false);
        b_send.setEnabled(false);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3));

        b_connect = new JButton("접속하기");
        b_disconnect = new JButton("접속 끊기");
        b_exit = new JButton("종료하기");
        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();

            }
        });

        b_disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });

        b_exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        panel.add(b_connect);
        panel.add(b_disconnect);
        panel.add(b_exit);

        return panel;
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            // t_display.append("서버에 접속했습니다.\n");
            connectGUI();
        } catch (IOException e) {
            t_display.append("서버 접속 오류: " + e.getMessage() + "\n");
        }
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {

                out.close();
                socket.close();
                t_display.append("서버와의 연결을 끊었습니다.\n");
                unconncetGUI();

            }
        } catch (IOException e) {
            t_display.append("연결 종료 오류: " + e.getMessage() + "\n");
        }
    }

    private void sendMessage() {
        String message = t_input.getText();
        if (message.isEmpty()) return;
        try {
            ((BufferedWriter) out).write(message + "\n");
            out.flush();
            t_display.append("나: " + message + "\n");
        } catch (IOException e) {
            System.err.println("error" + e.getMessage());
        }
        t_input.setText("");
    }

    private void printDisplay(String msg) {
        t_display.append(msg);
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    private void receiveMessage() {
        try {
            String inMsg = ((BufferedReader) in).readLine();
            printDisplay("서버:\t" + inMsg + "\n");
        } catch (IOException e) {
            System.err.println("error" + e.getMessage());
        }
    }

    private void unconncetGUI() {  //접속하기 종료하기만 클릭 가능
        b_connect.setEnabled(true);
        b_exit.setEnabled(true);
        b_disconnect.setEnabled(false);
        b_send.setEnabled(false);
        t_input.setEnabled(false);
    }

    private void connectGUI() {  //
        b_connect.setEnabled(false);
        b_exit.setEnabled(false);
        b_disconnect.setEnabled(true);
        b_send.setEnabled(true);
        t_input.setEnabled(true);
    }

    public static void main(String[] args) {
        new CalcClientGUI();

    }
}
