import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EchoServerGUI extends JFrame {
    private JTextArea t_display;  // t_display로 변경
    private JButton exitButton;
    private ServerSocket serverSocket;
    private int port;

    class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            receiveMessages(clientSocket);
        }
    }

    public EchoServerGUI(int port) {
        super("EchoServerGUI");
        buildGUI();
        this.setBounds(100, 200, 400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.port = port;
        startServer(port);
    }

    private void buildGUI() {
        this.setLayout(new BorderLayout());
        this.add(createDisplayPanel(), BorderLayout.CENTER);
        this.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        t_display = new JTextArea();  // t_display로 변경
        t_display.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(t_display);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        exitButton = new JButton("종료");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(-1);
            }
        });
        panel.add(exitButton);
        return panel;
    }
    public void startServer(int port) {

        try {
            serverSocket = new ServerSocket(port);
            printDisplay("서버가 시작되었습니다.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                EchoServerGUI.ClientHandler clientHandler = new EchoServerGUI.ClientHandler(clientSocket);
                printDisplay("클라이언트가 연결되었습니다.");
                // receiveMessages(clientSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            printDisplay("서버 오류: " + e.getMessage());
        }
    }
    private void receiveMessages(Socket cs) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), UTF_8));
            String message;
            while ((message = in.readLine()) != null) {
                printDisplay("클라이언트 메시지: " + message);
                out.write("!!" + message + "' ...echo" + "\n");
                out.flush();
            }
            printDisplay("클라이언트가 연결을 종료했습니다." + "\n");
        } catch (IOException e) {
            System.err.println("서버 읽기 오류: " + e.getMessage());
        } finally {
            try {
                cs.close();
            } catch (IOException e) {
                System.err.println("서버 닫기 오류: " + e.getMessage());
            }
        }
    }

    private void printDisplay(String msg) {
        t_display.append(msg + "\n");// t_display에 출력
        t_display.setCaretPosition(t_display.getDocument().getLength()); //자동 스크롤 다운
    }

    public static void main(String[] args) {
        new EchoServerGUI(54321);
    }
}