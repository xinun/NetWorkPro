import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class MsgClientGUI extends JFrame {
    private JTextArea t_display;
    private JTextField t_input;
    private JButton b_connect , b_disconnect, b_send, b_exit;
    private String serverAddress = "localhost";  // 서버 주소 설정
    private int serverPort = 54321;  // 서버 포트 설정
    private Socket socket;
    private Writer out;


    public MsgClientGUI() {
        super("MsgClientGUI");

        buildGUI();

        this.setBounds(800, 200, 400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        unconncetGUI(); //초기화를 위한 첨부터 실행될때부터 설정하기 위함

    }

    private void buildGUI() {
        this.setLayout(new BorderLayout());
        this.add(createDisplayPanel(), BorderLayout.CENTER);
        this.add(createInputAndControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        t_display = new JTextArea();
        t_display.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(t_display);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInputAndControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createInputPanel());
        panel.add(createControlPanel());
        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        t_input = new JTextField(20);
        b_send = new JButton("보내기");

        b_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageERROR();
            }
        });
        t_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageERROR();
            }
        });
        panel.add(t_input);
        panel.add(b_send);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());

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
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream()), "UTF-8")), true);
            t_display.append("서버에 접속했습니다.\n");
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

    private void sendMessage(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
            t_display.append("나: " + msg + "\n");
        } catch (IOException e) {
            t_display.append("메시지 전송 오류: " + e.getMessage() + "\n");
        }
    }


    private void sendMessageERROR() {
        String text = t_input.getText().trim();
        if (text.isEmpty()) {
            t_display.append("잘못된 입력입니다. 빈 메시지를 보낼 수 없습니다.\n");
            return;  // Return if the input is empty
        }

        try {
            sendMessage(text);
            t_input.setText("");
        } catch (NumberFormatException e) {
            t_display.append("잘못된 입력입니다. 정수를 입력해 주세요.\n");
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
        new MsgClientGUI();

    }
}
