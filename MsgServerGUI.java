import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MsgServerGUI extends JFrame {
    private JTextArea t_display;  // t_display로 변경
    private JButton exitButton;
    private ServerSocket serverSocket;

    class  ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            receiveMessages(clientSocket);
        }
    }
    public MsgServerGUI(int port) {
        super("MsgServerGUI");
        buildGUI();
        this.setBounds(100, 200, 400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
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
        JPanel panel = new JPanel(new FlowLayout());
        exitButton = new JButton("종료");
        exitButton.addActionListener(e -> System.exit(0));
        panel.add(exitButton);
        return panel;
    }

    public void startServer(int port) {

        try {
            serverSocket = new ServerSocket(port);
            printDisplay("서버가 시작되었습니다.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                printDisplay("클라이언트가 연결되었습니다.");
               // receiveMessages(clientSocket);
                clientHandler.start();

            }
        } catch (IOException e) {
            printDisplay("서버 오류: " + e.getMessage());
        }
    }


    private void receiveMessages(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"))) {
            String message;
            while ((message = in.readLine()) != null) {
                printDisplay("클라이언트 메시지: " + message);
            }
            printDisplay("클라이언트가 연결을 종료했습니다.");
        } catch (IOException e) {
            printDisplay("메시지 수신 오류: " + e.getMessage());
        }
    }

    private void printDisplay(String message) {
        t_display.append(message + "\n");// t_display에 출력
        t_display.setCaretPosition(t_display.getDocument().getLength()); //자동 스크롤 다운
    }

    public static void main(String[] args) {
        new MsgServerGUI(54321);

    }
}
