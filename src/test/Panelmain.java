package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Panelmain {
    public static int regisinfo;
    public static JFrame frame;
    private static String back;

    private static Socket so;

    static {
        try {
            so = new Socket("localhost", 8899);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static OutputStream os;
    static InputStream is;

    static {
        try {
            os = so.getOutputStream();
            is = so.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static PrintStream ps;

    static {
        try {
            ps = new PrintStream(new BufferedOutputStream(so.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static PrintWriter pw = new PrintWriter(os);//字符输出流
    static BufferedWriter bw = new BufferedWriter(pw);//加上缓冲流
    static InputStreamReader isr = new InputStreamReader(is);
    static BufferedReader br = new BufferedReader(isr);

    public static void main(String[] args) {
        // 创建 JFrame 实例
        frame = new JFrame("个人云空间登录页");
        // Setting the width and height of frame
        frame.setSize(400, 200);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */
        JPanel panel = new JPanel(null);
        // 添加面板
        frame.add(panel);
        /*
         * 调用用户定义的方法并添加组件到面板
         */
        placeComponents(panel);

        // 设置界面可见
        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {

        /* 布局部分我们这边不多做介绍
         * 这边设置布局为 null
         */
        panel.setLayout(null);

        JLabel gra = new JLabel(new ImageIcon("img/cloud2.png"));
        panel.add(gra);
        gra.setBounds(20, 30, 100, 100);

        // 创建 JLabel
        JLabel userLabel = new JLabel("设备:");
        userLabel.setBounds(140, 40, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(200, 40, 160, 25);
        panel.add(userText);
        // 输入密码的文本域
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(140, 70, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(200, 70, 160, 25);
        panel.add(passwordText);

        // 创建登录按钮
        JButton loginButton = new JButton("登录");
        loginButton.setBounds(150, 110, 80, 25);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String uname = userText.getText();
                String pword = passwordText.getText();

                Client cli = new Client(bw, br, ps);
                back = cli.main(uname, pword);
                if (back.equals("101")) {
                    showCustomDialog(frame, frame);
                    frame.dispose();
                    Function f1 = new Function(bw, br, ps);
                    try {
                        f1.main(uname);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    showCustomDialog(frame, frame);
                }
            }
        });
        panel.add(loginButton);

        JButton signUpButton = new JButton("注册");
        signUpButton.setBounds(240, 110, 80, 25);
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String uname = userText.getText();
                String pword = passwordText.getText();

                ps.println("600 " + uname + " " + pword);
                ps.flush();

                String info = "";
                try {
                    info = br.readLine();
                    System.out.println(info);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                System.out.println(info);
                if (info.equals("601")){
                    Function.showCustomDialog(frame,frame,"注册成功");
                    File file = new File("D:\\" + uname);
                    if (!file.exists()){
                        file.mkdir();
                    }
                } else if(info.equals("603")) {
                    Function.showCustomDialog(frame,frame,"拒绝注册");
                } else {
                    Function.showCustomDialog(frame,frame,"已存在该设备");
                }
            }
        });
        panel.add(signUpButton);
    }

    private static void showCustomDialog(Frame owner, Component parentComponent) {
        // 创建一个模态对话框
        final JDialog dialog = new JDialog(owner, "提示", true);
        // 设置对话框的宽高
        dialog.setSize(250, 150);
        // 设置对话框大小不可改变
        dialog.setResizable(false);
        // 设置对话框相对显示的位置
        dialog.setLocationRelativeTo(parentComponent);

        // 创建一个标签显示消息内容
        JLabel messageLabel;
        if (back.equals("101")) {
            messageLabel = new JLabel("登录成功");
        } else {
            messageLabel = new JLabel("登录失败");
        }
        // 创建一个按钮用于关闭对话框
        JButton okBtn = new JButton("确定");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                dialog.dispose();
            }
        });
        // 创建对话框的内容面板, 在面板内可以根据自己的需要添加任何组件并做任意是布局
        JPanel panel = new JPanel();
        // 添加组件到面板
        panel.add(messageLabel);
        panel.add(okBtn);
        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        // 显示对话框
        dialog.setVisible(true);
    }
}