import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class CALCSERVERTEST extends JFrame {
    private JTextArea t_display;
    private JButton exitButton;
    private ServerSocket serverSocket;

    public CALCSERVERTEST(int port) {
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
        t_display = new JTextArea();
        t_display.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(t_display);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        exitButton = new JButton("종료");
        exitButton.addActionListener(e -> System.exit(0));
        panel.add(exitButton);
        return panel;
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            printDisplay("서버가 시작되었습니다. 포트: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                printDisplay("클라이언트가 연결되었습니다.");
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            printDisplay("서버 오류: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            CalcExpr calcExpr = (CalcExpr) in.readObject();
            double result = calculateResult(calcExpr);

            out.writeObject(result);
            out.flush();

            printDisplay("계산 결과: " + result);

        } catch (IOException | ClassNotFoundException e) {
            printDisplay("클라이언트 처리 중 오류 발생: " + e.getMessage());
        }
    }

    private double calculateResult(CalcExpr calcExpr) {
        switch (calcExpr.operator) {
            case '+':
                return calcExpr.operand1 + calcExpr.operand2;
            case '-':
                return calcExpr.operand1 - calcExpr.operand2;
            case '*':
                return calcExpr.operand1 * calcExpr.operand2;
            case '/':
                return (calcExpr.operand2 != 0) ? calcExpr.operand1 / calcExpr.operand2 : Double.NaN;
            default:
                return Double.NaN;
        }
    }

    private void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args) {
        new CALCSERVERTEST(54321);
    }
}
