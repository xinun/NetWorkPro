package server;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ImgReceiver extends JFrame {
    //  private JTextArea t_display;
    private JTextPane t_display;
    private DefaultStyledDocument document;

    private JButton exitButton;
    private ServerSocket serverSocket;
    private int port;
    private Thread acceptThread=null;
    class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }



        private void receiveImage(Socket cs) {
            try {
                DataInputStream in = new DataInputStream(cs.getInputStream());
                while(true){
                    String fileName = in.readUTF();
                    long size = (int)in.readLong();
                    File file = new File(fileName);
                    BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(file));
                    byte[] buffer = new byte[1024];
                    int nRead;
                    while(size>0) {
                        nRead= in.read(buffer);
                        size-=nRead;
                        bos.write(buffer,0,nRead);
                    }
                    bos.close();
                    printDisplay("수신 완료"+file.getName());

                    printDisplay("클라이언트가 연결을 종료했습니다." + "\n");
                    ImageIcon icon = new ImageIcon(file.getName());

                    printDisplay(icon);
                }
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
        @Override
        public void run(){
            receiveImage(clientSocket);
        }
    }

    public ImgReceiver(int port) {
        super("ImgReceiver");

        buildGUI();
        this.setBounds(100, 200, 400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.port = port;
        //startServer(port);
        acceptThread= new Thread(new Runnable() {
            public void run() {
                startServer(port);
            }
        });
        acceptThread.start();
    }

    private void buildGUI() {
        this.setLayout(new BorderLayout());
        this.add(createDisplayPanel(), BorderLayout.CENTER);
        this.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel p = new JPanel(new BorderLayout());
        document = new DefaultStyledDocument();
        t_display = new JTextPane();

        t_display.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(t_display);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        p.add(scrollPane, BorderLayout.CENTER);
        return p;
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
                ImgReceiver.ClientHandler clientHandler = new ImgReceiver.ClientHandler(clientSocket);
                printDisplay("클라이언트가 연결되었습니다.");
                clientHandler.start();
            }
        } catch (IOException e) {
            printDisplay("서버 오류: " + e.getMessage());
        }
    }

    private void printDisplay(String msg) {
        int len = t_display.getDocument().getLength();
        try{
            document.insertString(len,msg +"\n",null);

        }catch (BadLocationException e) {
            e.printStackTrace();
        }
        t_display.setCaretPosition(len);
    }
    private void printDisplay(ImageIcon icon){
        t_display.setCaretPosition(t_display.getDocument().getLength());
        if(icon.getIconWidth()>400){
            Image img = icon.getImage();
            Image changeImg=img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
            icon = new ImageIcon(changeImg);
        }
        t_display.insertIcon(icon);
        printDisplay("");
    }
    public static void main(String[] args) {
        new ImgReceiver(54321);
    }
}
