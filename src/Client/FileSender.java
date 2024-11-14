package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class FileSender extends JFrame {

    private JTextField t_input;
    private JTextArea t_display;
    private JButton b_connect;
    private JButton b_disconnect;
    private JButton b_send;
    private JButton b_exit;
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private OutputStream out;
    //private Reader in;
    private boolean Connected = false;

    public FileSender(String serverAddress, int serverPort) {
        super("FileSender");

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
            //out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"),true);
            out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            t_input.setEditable(true);
            b_send.setEnabled(true);
            b_disconnect.setEnabled(true);
            b_connect.setEnabled(false);
            b_exit.setEnabled(false);

            t_display.append("서버에 접속되었습니다.\n");
        } catch (IOException e) {
            System.err.println("알 수 없는 서버> " + e.getMessage());
            System.exit(-1);
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
        String message = t_input.getText();
        if (message.isEmpty()) return;
        try {
            ((DataOutputStream) out).writeUTF(message);
            out.flush();
            t_display.append("나" + message + "\n");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
    private void sendFile(){
        String filename = t_input.getText().strip();
        if(filename.isEmpty()) return;
        File file = new File(filename);
        if(!file.exists()){
            t_display.append("파일이 존재하지 않습니다 : "+ filename + "\n");
            return;
        }
        BufferedInputStream bis = null;
        try {
            bis= new BufferedInputStream(new FileInputStream(file));
            ((DataOutputStream) out).writeUTF(file.getName());

            byte[] buffer = new byte[1024];
            int nRead;
            while((nRead = bis.read(buffer))!= -1)
            {out.write(buffer,0,nRead);}
            out.close();
        }catch (UnsupportedEncodingException e){
            t_display.append(" 인코딩 형식을 알 수 없음"+e.getMessage()+"\n");
            return;
        }catch (FileNotFoundException e){
            t_display.append("파일이 존재 하지 않습니다"+e.getMessage()+"\n");
            return;
        }catch (IOException e){
            t_display.append("파일이 존재 하지 않습니다"+e.getMessage()+"\n");
            return;
        }finally {
            try{
                if(bis != null) bis.close();
            }catch (IOException e){
                printDisplay(">> 파일 닫기 오류"+ e.getMessage());
                return;
            }
        }

    }

    private void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }
/*    private void receiveMessage() {

        try {
            String inMsg=((BufferedReader)in).readLine();
            t_display.append("서버:\t"+inMsg+"\n");
        } catch (IOException e) {
            System.err.println("클라이언트 일반 수신 오류> " + e.getMessage());
            System.exit(-1);
        }

    }*/



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

        JPanel p = new JPanel(new BorderLayout());
        b_connect = new JButton("접속하기");
        b_disconnect = new JButton("접속 끊기");
        b_disconnect.setEnabled(false);
        b_exit = new JButton("종료하기");

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //sendMessage();
                sendFile();
            }
        };
        t_input = new JTextField(30);
        t_input.addActionListener(listener);
        b_send = new JButton("보내기");
        b_send.addActionListener(listener);
        p.add(t_input,BorderLayout.CENTER);
        p.add(b_send,BorderLayout.EAST);
        t_input.setEditable(false);
        b_send.setEnabled(false);
        return p;
    }



    // control 패널
    private JPanel createControlPanel() {
        JPanel p = new JPanel(new GridLayout());

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

        p.add(b_connect);
        p.add(b_disconnect);
        p.add(b_exit);

        return p;
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // 연결하고자하는 서버의 주소는 로컬호스트. 즉, 내 컴퓨터.
        int serverPort = 54321;

        FileSender client = new FileSender(serverAddress, serverPort);
    }

}