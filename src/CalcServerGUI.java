//1971068 전지훈


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;
public class CalcServerGUI extends JFrame {
    private JTextArea t_display;
    private final JFrame frame;
    private final int port;
    private JButton exitButton;


    private ServerSocket serverSocket;
    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            receiveMessages(clientSocket);
        }
    }
    public CalcServerGUI(int port) {
        this.port = port;

        frame = new JFrame("CalcServerGUI");

        buildGUI();

        frame.setSize(400, 300);
        frame.setLocation(900, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        startServer(port);

    }




    private void buildGUI() {
        frame.add(createDisplayPanel(), BorderLayout.CENTER);
        frame.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);

        t_display = new JTextArea();
        t_display.setEditable(false);

        scrollPane.setViewportView(t_display);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0));

        exitButton = new JButton("종료");
        panel.add(exitButton);

        // Event Listeners
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    System.err.println("서버 닫기 오류: " + ex.getMessage());
                    System.exit(-1);
                }
                System.exit(0);
            }
        });

        return panel;
    }


    private void startServer(int port) {
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            t_display.append("서버가 시작되었습니다.\n");

            while (true) {
                clientSocket = serverSocket.accept();
                t_display.append("클라이언트가 연결되었습니다.\n");

                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("서버 닫기 오류: " + e.getMessage());
            }
        }
    }



    private void receiveMessages(Socket cs) {
        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(cs.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(cs.getOutputStream()));

            CalcExpr message;
            try {
                while ((message = (CalcExpr) in.readObject()) != null) {
                    double result = calc(message);

                    printDisplay(message + " = " + result + "\n");
                    out.writeDouble(result);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("메시지 수신 오류: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("클래스 찾기 오류: " + e.getMessage());
            }

            printDisplay("클라이언트가 연결을 종료했습니다.\n");
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
//CalcExpr 이용하여 결과 받기
    private double calc(CalcExpr expr) {
        double result;
        switch (expr.operator) {
            case '+':
                result = expr.operand1 + expr.operand2;
                break;
            case '-':
                result = expr.operand1 - expr.operand2;
                break;
            case '*':
                result = expr.operand1 * expr.operand2;
                break;
            case '/':
                result = expr.operand1 / expr.operand2;
                break;
            default:
                result = 0;
        }
        return result;
    }

    private void printDisplay(String msg) {
        t_display.append(msg + "\n");// t_display에 출력
        t_display.setCaretPosition(t_display.getDocument().getLength()); //자동 스크롤 다운
    }
    public static void main(String[] args) {
        new CalcServerGUI(54321);
    }
}