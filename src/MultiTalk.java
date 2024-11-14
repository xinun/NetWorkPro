import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class MultiTalk extends JFrame {
    private String serverAddress;
    private int serverPort;
    private Writer out;
    private Reader in;
    JButton b_connect, b_disconnect, b_exit;
    JTextArea t_display;
    JTextField t_input;
    JTextField t_userID;
    JTextField t_hostAddr;
    JTextField t_portNum;
    Socket socket;
    private Thread receiveThread = null;

    public MultiTalk(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        this.setBounds(500, 200, 500, 500);
        this.setTitle("MultiTalk");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);
        JPanel p_input = new JPanel(new GridLayout(3, 0)); // 아래에 갈 패널 준비
        p_input.add(createInputPanel());
        p_input.add(createControlPanel());
        p_input.add(createInfoPanel());
        this.add(p_input, BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        t_display = new JTextArea();
        t_display.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(t_display), BorderLayout.CENTER);

        return panel;
    }

    private void sendUserID() {
        String uid = t_userID.getText();
        try {
            ((BufferedWriter) out).write("/uid:" + uid + "\n");
            out.flush();
        } catch (IOException e) {
            System.out.println("클라이언트 일반 전송 오류>" + e.getMessage());
            System.exit(-1);
        }
        t_input.setText("");
    }

    private JPanel createControlPanel() {
        b_connect = new JButton("접속하기");

        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //MultiTalk.this 안 써줘도 되는데 명시적으로 쓸 거면 다 써야 된다. 그냥 this는 외부에 있는 클래스 MultiTalk를 지칭하지 않고 이 함수 내부를 의미하기 때문.
                MultiTalk.this.serverAddress = t_hostAddr.getText();
                MultiTalk.this.serverPort = Integer.parseInt(t_portNum.getText());

                try {
                    connectToServer();
                    sendUserID();
                } catch (UnknownHostException e1) {
                    printDisplay("서버 주소와 포트 번호를 확인하세요 : " + e1.getMessage());
                    //버튼 상태 안 바꾸고 빠져나옴
                    return;
                } catch (IOException e1) {
                    printDisplay("서버와의 연결 오류 : " + e1.getMessage());
                    return;
                }

                b_connect.setEnabled(false);
                b_disconnect.setEnabled(true);

                t_input.setEnabled(true);
                b_exit.setEnabled(false);

                t_userID.setEditable(false);
                t_hostAddr.setEditable(false);
                t_portNum.setEditable(false);


            }
        });

        b_disconnect = new JButton("접속 끊기");
        b_disconnect.setEnabled(false); // 처음엔 비활성화
        b_disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                disconnect();
            }
        });

        b_exit = new JButton("종료하기");
        b_exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 3));

        panel.add(b_connect);
        panel.add(b_disconnect);
        panel.add(b_exit);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        t_userID = new JTextField(7);
        t_hostAddr = new JTextField(12);
        t_portNum = new JTextField(5);
        t_userID.setText("guest" + getLocalAddr().split("\\.")[3]);
        t_hostAddr.setText(this.serverAddress);
        t_hostAddr.setText(String.valueOf(this.serverPort));
        t_portNum.setHorizontalAlignment(JTextField.CENTER);
        p.add(new JLabel("아이디:"));
        p.add(t_userID);
        p.add(new JLabel("서버주소:"));
        p.add(t_hostAddr);
        p.add(new JLabel("포트번호:"));
        p.add(t_portNum);
        return p;
    }

    private String getLocalAddr() {
        InetAddress local = null;
        String addr = "";
        try {
            local = InetAddress.getLocalHost();
            addr = local.getHostAddress();
            System.out.println(addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr;
    }

    private JPanel createInputPanel() { // 두 번째 단 입력창과 보내기 버튼
        t_input = new JTextField(30);
        t_input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
                // receiveMessage();
            }
        });

        JButton b_send = new JButton("보내기");
        b_send.addActionListener(new ActionListener() { // 이벤트 리스너
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
                //receiveMessage();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(t_input, BorderLayout.CENTER);
        panel.add(b_send, BorderLayout.EAST);

        return panel;
    }

    private void connectToServer() throws UnknownHostException, IOException {

        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        socket.connect(sa, 3000);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    receiveMessage();
                }
            }
        });
        receiveThread.start();

        b_connect.setEnabled(false);
        b_disconnect.setEnabled(true);
        b_exit.setEnabled(false);
    }

    private void sendMessage() {
        String message = t_input.getText();

        if (message.isEmpty()) return;
        else {
            try {
                ((BufferedWriter) out).write(message + '\n');
                out.flush();
            } catch (IOException e) {
                System.err.println("클라이언트 쓰기 오류 > " + e.getMessage());
                System.exit(-1);
            }
            t_input.setText(""); // 보낸 후 입력창은 비우기
        }
    }

    private void receiveMessage() {
        try {
            String inMsg = ((BufferedReader) in).readLine();
            if (inMsg == null) {
                disconnect();
                printDisplay("서버 연결 끊김");
                return;
            }
            printDisplay(inMsg);
        } catch (IOException e) {
            System.err.println("클라이언트 일반 수신 오류 > " + e.getMessage());
        }
    }
    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }
    private void disconnect() {
        try {
            receiveThread = null;
            socket.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류 > " + e.getMessage());
            System.exit(-1);
        }
        b_connect.setEnabled(true);
        b_disconnect.setEnabled(false);
        b_exit.setEnabled(true);
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;

        MultiTalk client = new MultiTalk(serverAddress, serverPort);
    }
}

