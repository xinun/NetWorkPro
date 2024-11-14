
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.*;

public class EchoClientGUI extends JFrame {

    private JTextField t_input;
    private JTextArea t_display;
    private JButton b_connect;
    private JButton b_disconnect;
    private JButton b_send;
    private JButton b_exit;
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private Writer out;
    private Reader in;
    private boolean Connected = false;

    public EchoClientGUI(String serverAddress, int serverPort) {
        super("EchoClient GUI");

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        setBounds(300, 300, 500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void connectToServer() {

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));

            Connected = true;
        } catch (UnknownHostException e) {
            System.err.println("알 수 없는 서버> " + e.getMessage());
        }
        catch (IOException e) {
            System.err.println("클라이언트 연결 오류> " + e.getMessage());
        }

    }

    // 서버와의 연결 종료

    private void disconnect() {

        try {
            out.close();
            socket.close();
            Connected = false;

            b_send.setEnabled(false);
            b_connect.setEnabled(true);
            b_exit.setEnabled(true);
            b_disconnect.setEnabled(false);

        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류> " + e.getMessage());
            System.exit(-1);
        }


    }

    private void sendMessage() {

        if (!Connected)
            return;

        String msg = t_input.getText();

        if (msg.equals(""))
            return;


        try {
            ((BufferedWriter)out).write(msg+"\n");
            out.flush();
        } catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
            System.exit(-1);
        }

        t_display.append("나: " + msg + "\n");
        t_input.setText("");

        receiveMessage();
    }

    private void receiveMessage() {

        try {
            String inMsg=((BufferedReader)in).readLine();
            t_display.append("서버:\t"+inMsg+"\n");
        } catch (IOException e) {
            System.err.println("클라이언트 일반 수신 오류> " + e.getMessage());
            System.exit(-1);
        }

    }



    private void buildGUI() {
        JPanel dPanel = createDisplayPanel();
        JPanel icPanel = new JPanel(new GridLayout(2, 1));
        JPanel iPanel = createInputPanel();
        JPanel cPanel = createControlPanel();

        icPanel.add(iPanel);
        icPanel.add(cPanel);

        add(dPanel, BorderLayout.CENTER);
        add(icPanel, BorderLayout.SOUTH);
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


    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        t_input = new JTextField(20);
        b_send = new JButton("보내기");

        b_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        t_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        panel.add(t_input);
        panel.add(b_send);

        return panel;
    }


    // control 패널
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout());
        b_connect = new JButton("접속하기");
        b_disconnect = new JButton("접속 끊기");
        b_disconnect.setEnabled(false);
        b_exit = new JButton("종료하기");

        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                b_send.setEnabled(true);
                b_connect.setEnabled(false);
                b_exit.setEnabled(false);
                b_disconnect.setEnabled(true);
                connectToServer(); // 접속하기버튼 클릭시 서버에 접속요청.
            }
        });

        b_disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                disconnect(); // 접속끊기버튼 클릭시 서버와 연결종료.

            }
        });

        b_exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // 프로그램 정상 종료.
            }
        });

        controlPanel.add(b_connect);
        controlPanel.add(b_disconnect);
        controlPanel.add(b_exit);

        return controlPanel;
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // 연결하고자하는 서버의 주소는 로컬호스트. 즉, 내 컴퓨터.
        int serverPort = 54321;

        EchoClientGUI client = new EchoClientGUI(serverAddress, serverPort);
    }

}