package com.rolling.sever;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;


public class Sever {
    //记录连接通道
    static ArrayList<Socket> list = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(10001);
        //读取本地已注册用户信息
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream("servicedir\\userinfo.text");
        prop.load(fis);
        fis.close();

        //循环创建新登录线程
        while (true) {
            Socket socket = ss.accept();
            Sever.list.add(socket);
            System.out.println("有新的用户连接上服务器");
            new Thread(new MyRunable(socket, prop)).start();
        }
    }
}

class MyRunable implements Runnable {
    Socket socket;
    Properties prop;

    public MyRunable(Socket socket, Properties prop) {
        this.socket = socket;
        this.prop = prop;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String choose = br.readLine();
                switch (choose) {
                    case "login" -> login(br);
                    case "register" -> register(br);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     *
     * 注册代码实现
     *
     * */
    private void register(BufferedReader br) throws IOException {
        String userinfo = br.readLine();
        String[] arr = userinfo.split("&");
        //解析获取用户名和密码
        String username = arr[0].split("=")[1];
        String password = arr[1].split("=")[1];

        //检查用户名和密码符合规则，并发送给客户端
        //0: 注册成功 1：用户名重复 2：用户名不符合要求 3：密码不符合要求
        if (!username.matches("[a-zA-Z]{6,18}")) {
            writeMessage2Client("2");
        } else if (prop.containsKey(username)) {
            writeMessage2Client("1");
        } else if (!password.matches("[a-zA-Z]\\d{2,7}")) {
            writeMessage2Client("3");
        } else {
            prop.put(username, password);
            FileOutputStream fos = new FileOutputStream("servicedir\\userinfo.text");
            prop.store(fos, "");
            writeMessage2Client("0");
        }
    }

    private void login(BufferedReader br) {
        try {
            String userinfo = br.readLine();
            String[] arr = userinfo.split("&");
            //解析获取用户名和密码
            String username = arr[0].split("=")[1];
            String password = arr[1].split("=")[1];

            //判断用户名和密码是否存在，发送给客户端
            if (prop.containsKey(username)) {
                if (prop.containsValue(password)) {
                    writeMessage2Client("1");
                    //接收客户端发送的消息，并打印在控制台
                    talk2All(br, username);
                } else {
                    writeMessage2Client("2");
                }
            } else {
                writeMessage2Client("3");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void talk2All(BufferedReader br, String username) throws IOException {
        while (true) {
            String message = br.readLine();
            System.out.println(username + "发送过来消息：" + message);


            //给每个用户发送信息
            for (Socket socket : Sever.list) {
                writeMessage2Client(socket, username + "发送过来消息：" + message);
            }
        }
    }

    /*
     * 向客户端发送信息
     * */
    public void writeMessage2Client(String message) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bw.write(message);
        bw.newLine();
        bw.flush();
    }

    public void writeMessage2Client(Socket s, String message) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        bw.write(message);
        bw.newLine();
        bw.flush();
    }
}
