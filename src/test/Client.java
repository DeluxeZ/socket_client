package test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public static String regisinfo; //登录验证信息
    static BufferedWriter bw = null; 
    static BufferedReader br = null;
    static PrintStream ps = null;

    //Client构造函数
    public Client(BufferedWriter bw, BufferedReader br, PrintStream ps) {
        this.bw = bw;
        this.br = br;
        this.ps = ps;
    }

    public static String main(String args, String args2) {
        String backinfo=null;
        try {
            String usrname = args;
            String passwrd = args2;

            //2.获取输出流，向服务器端发送登录的信息
            ps.println("100 " + usrname + " " + passwrd);
//            bw.newLine();
            ps.flush();
//            so.shutdownOutput();//关闭输出流

            //3.获取输入流，得到服务端的响应信息
            if ((regisinfo = br.readLine()) != null) {
                System.out.println("我是客户端，服务器说:" + regisinfo);
                backinfo = regisinfo;
            }

        } catch (java.io.IOException e) {
            System.out.println("无法连接 ");
            System.out.println(e);
        }
        return backinfo;
    }
}