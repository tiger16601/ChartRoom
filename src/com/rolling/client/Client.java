package com.rolling.client;

import javax.management.remote.JMXServerErrorException;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 10001);
        System.out.println("服务器已经连接成功");
        while (true) {
            System.out.println("==============欢迎来到黑马聊天室================");
            System.out.println("1登录");
            System.out.println("2注册");
            System.out.println("请输入您的选择：");
            Scanner sc = new Scanner(System.in);
            String choose = sc.nextLine();
            switch (choose) {
                case "1" -> login(socket);
                case "2" -> register(socket);
                default -> System.out.println("没有这个选项，请重新输入！");
            }
        }
    }

    private static void register(Socket socket) throws IOException {
        //获取输出流
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //给服务器发送登录指令
        WriteMessage2Sever(bw, "register");

        //给客户端发送用户名和密码
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名");
        String username = sc.nextLine();
        System.out.println("请输入密码");
        String password = sc.nextLine();

        //username=zhangsan&password=123
        StringBuilder sb = new StringBuilder();
        sb.append("username").append("=").append(username).append("&").append("password").append("=").append(password);
        WriteMessage2Sever(bw, sb.toString());

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String sign = br.readLine();
        if("2".equals(sign)){
            System.out.println("用户名不符合要求");
        }else if ("1".equals(sign)){
            System.out.println("用户名重复");
        }else if ("3".equals(sign)){
            System.out.println("密码不符合要求");
        }else if ("0".equals(sign)){
            System.out.println("注册成功，请重新登录");
        }
    }

    public static void login(Socket socket) throws IOException {
        //获取输出流
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //给服务器发送登录指令
        WriteMessage2Sever(bw, "login");

        //给客户端发送用户名和密码
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名");
        String username = sc.nextLine();
        System.out.println("请输入密码");
        String password = sc.nextLine();

        //username=zhangsan&password=123
        StringBuilder sb = new StringBuilder();
        sb.append("username").append("=").append(username).append("&").append("password").append("=").append(password);
        WriteMessage2Sever(bw, sb.toString());

        //接收服务器比对结果
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String sign = br.readLine();
        //1:登录成功 2：密码有误 3：用户名不存在
        if ("1".equals(sign)) {
            System.out.println("登录成功");
            //运行登录后代码
            //创建新线程，负责接收服务端的聊天记录
            new Thread(new ClienMyRunnable(socket)).start();
            //开始聊天
            talk2All(bw);
        } else if ("2".equals(sign)) {
            System.out.println("密码输入错误");
            //返回主界面重新录入

        } else if ("3".equals(sign)) {
            System.out.println("用户名不存在");
            //返回主界面重新录入

        }
    }

    private static void WriteMessage2Sever(BufferedWriter bw, String message) throws IOException {
        bw.write(message);
        bw.newLine();
        bw.flush();
    }


    //往服务器写出消息
    private static void talk2All(BufferedWriter bw) throws IOException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("请输入您要说的话：");
            String message = sc.nextLine();
            WriteMessage2Sever(bw, message);
        }
    }
}
/*
*
* 接收服务器发送的聊天消息
*
* */
class ClienMyRunnable implements Runnable {
    Socket socket;

    public ClienMyRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = br.readLine();
                System.out.println(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}