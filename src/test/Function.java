package test;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

public class Function {
    public static JFrame func; //主要页面
    public static JTable table; //资源信息显示表
    private static Vector<String> dataTitle = new Vector<String>();//表格列名
    private static Vector<Vector<String>> data = new Vector<Vector<String>>();//表格单元格内容
    private static int selectedRow; //选中的资源列
    private static String uname; //设备名
    static BufferedWriter bw = null;
    static BufferedReader br = null;
    static PrintStream ps = null;
    static TransServer ts = null;
    static TransClient tc = null;
    static String filename = "";
    static String addp = "";

    //构造函数
    public Function(BufferedWriter bw, BufferedReader br, PrintStream ps) {
        this.bw = bw;
        this.br = br;
        this.ps = ps;
    }

    public static void main(String args) throws IOException {
        uname = args;
        func = new JFrame("个人云空间功能页");
        func.setSize(800, 500);
        func.setResizable(false);
        func.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(null);
        func.add(panel);
        placeComponents(panel);
        func.setVisible(true);

        //创建一个线程来监听服务端发来的信息
        new Thread() {
            @Override
            public void run() {
                super.run();
                String info;
                try {
                    //循环监听所有消息
                    while ((info = br.readLine()) != null) {
                        System.out.println(info);
                        // 将后端传回来的字符串分割
                        String[] da = info.split("/");
                        //根据传回来不同的标志位信息选择不同的执行体
                        if (da[0].equals("401")) {
                            Refresh(da);
                        } else if (da[0].equals("301")) {
                            showCustomDialog(func, func, "声明成功");
                            copyFile(addp, uname);
                        } else if (da[0].equals("302")) {
                            showCustomDialog(func, func, "声明失败");
                        } else if (da[0].equals("501")) {
                            showCustomDialog(func, func, "退出成功");
                            ps.close();
                            try {
                                br.close();
                                bw.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            func.dispose();
                        } else if (da[0].equals("202")) {
                            showCustomDialog(func, func, "设备不在线");
                            ts.close();
                        } else if (da[0].equals("203")) {
                            String receiveIP = da[1];
                            String filename = da[2];
                            System.out.println(receiveIP + " " + filename);
                            tc = new TransClient(receiveIP, filename, uname);
                            int re = tc.sendFile();
                            if (re == 1) {
                                showCustomDialog(func, func, "传输成功");
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }.start();
    }

    //复制文件函数，用于申明资源的时候将文件复制到共享资源文件夹中
    public static void copyFile(String srcpath, String uname) {
        // System.out.println(srcpath);
        // System.out.println(uname);
        File src = new File(srcpath);
        File dest = new File("D:\\" + uname + "\\" + filename);
        // 定义文件输入流和输出流对象
        FileInputStream fis = null;// 输入流
        FileOutputStream fos = null;// 输出流

        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            byte[] bs = new byte[1024];
            while (true) {
                int len = fis.read(bs, 0, bs.length);
                if (len == -1) {
                    break;
                } else {
                    fos.write(bs, 0, len);
                }
            }
            System.out.println("复制文件成功");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //创建gui主体部分，即定义每一个按键的函数
    private static void placeComponents(JPanel panel) {
        JLabel sourcelist = new JLabel("资源列表");
        sourcelist.setBounds(10, 10, 120, 25);
        panel.add(sourcelist);
        dataTitle.add("资源名称");
        dataTitle.add("所在设备");
        dataTitle.add("是否可用");
        dataTitle.add("备注");
        dataTitle.add("所在设备");
        // 创建一个表格，指定 表头 和 所有行数据
        DefaultTableModel newTableModel = new DefaultTableModel(data, dataTitle) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(newTableModel);
        Refresh();
//        table.updateUI();
//        data.removeAllElements();

        // 设置表格内容颜色
        table.setForeground(Color.BLACK);                   // 字体颜色
        table.setFont(new Font(null, Font.PLAIN, 14));      // 字体样式
        table.setSelectionForeground(Color.DARK_GRAY);      // 选中后字体颜色
        table.setSelectionBackground(Color.LIGHT_GRAY);     // 选中后字体背景
        table.setGridColor(Color.GRAY);                     // 网格颜色
        // 设置表头
        table.getTableHeader().setFont(new Font(null, Font.BOLD, 14));  // 设置表头名称字体样式
        table.getTableHeader().setForeground(Color.RED);                // 设置表头名称字体颜色
        table.getTableHeader().setResizingAllowed(false);               // 设置不允许手动改变列宽
        table.getTableHeader().setReorderingAllowed(false);             // 设置不允许拖动重新排序各列
        // 设置行高
        table.setRowHeight(30);
        // 第一列列宽设置为40
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        // 设置滚动面板视口大小（超过该大小的行数据，需要拖动滚动条才能看到）
        table.setPreferredScrollableViewportSize(new Dimension(600, 300));

        // 把 表格 放到 滚动面板 中（表头将自动添加到滚动面板顶部）
        JScrollPane scrollPane = new JScrollPane(table);

        // 添加 滚动面板 到 内容面板
        scrollPane.setBounds(10, 40, 600, 400);
        panel.add(scrollPane);

        //添加声明资源按键及绑定响应事件
        JButton openBtn = new JButton("声明资源");
        openBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addp = showFileOpenDialog(table, scrollPane);
                if (addp != "") {
//                    String[] array = addp.split("\\\\");
//                    String pathway = array[array.length - 1];
                    try {
                        additem(addp);
                    } catch (UnknownHostException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    while (model.getRowCount() > 0) {
                        model.removeRow(model.getRowCount() - 1);
                    }
                    Refresh();
                    table.updateUI();
//                    data.removeAllElements();
                }
            }
        });
        openBtn.setBounds(650, 100, 100, 25);
        panel.add(openBtn);


        //创建请求资源按键及绑定响应函数
        JButton loadBtn = new JButton("请求资源");
        ListSelectionModel selectionModel = table.getSelectionModel();
        int selectionMode = ListSelectionModel.SINGLE_SELECTION;

        selectionModel.setSelectionMode(selectionMode);
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent g) {
                // 获取选中的第一行
                selectedRow = table.getSelectedRow();
            }
        });
        loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(selectedRow);
                String getName = String.valueOf(table.getValueAt(selectedRow, 0));
                String sendName = String.valueOf(table.getValueAt(selectedRow, 1));
                String sendIP = String.valueOf(table.getValueAt(selectedRow, 4));
                try {
                    getSource(getName, sendName, sendIP);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        loadBtn.setBounds(650, 150, 100, 25);
        panel.add(loadBtn);

        //创建刷新列表按键并绑定响应函数
        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent h) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                while (model.getRowCount() > 0) {
                    model.removeRow(model.getRowCount() - 1);
                }
                Refresh();

                table.updateUI();
//                data.removeAllElements();
                showCustomDialog(func, func, "刷新成功");
            }
        });
        refreshBtn.setBounds(650, 200, 100, 25);
        panel.add(refreshBtn);

        //创建登出按键并绑定响应函数
        JButton outBtn = new JButton("登出");
        outBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent o) {
                try {
                    leave(uname);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        outBtn.setBounds(650, 250, 100, 25);
        panel.add(outBtn);

        //背景图片
        JLabel gra = new JLabel(new ImageIcon("img/cloud2.png"));
        panel.add(gra);
        gra.setBounds(650, 300, 100, 100);
    }

    //文件选择器，用于申明资源选择时选择文件
    private static String showFileOpenDialog(Component parent, JScrollPane msgTextArea) {
        // 创建一个默认的文件选取器
        JFileChooser fileChooser = new JFileChooser();

        // 设置默认显示的文件夹为当前文件夹
        fileChooser.setCurrentDirectory(new File("."));

        // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 设置是否允许多选
        fileChooser.setMultiSelectionEnabled(false);

        // 添加可用的文件过滤器（FileNameExtensionFilter 的第一个参数是描述, 后面是需要过滤的文件扩展名 可变参数）
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("zip(*.zip, *.rar)", "zip", "rar"));

        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        int result = fileChooser.showOpenDialog(parent);
        String path = "";

        if (result == JFileChooser.APPROVE_OPTION) {
            // 如果点击了"确定", 则获取选择的文件路径
            File file = fileChooser.getSelectedFile();

            // 如果允许选择多个文件, 则通过下面方法获取选择的所有文件
            // File[] files = fileChooser.getSelectedFiles();

            path = file.getAbsolutePath();
//            System.out.println(path);
        }
        return path;
    }

    //刷新列表的函数，调用此函数将向服务器发送刷新请求
    private static void Refresh() {
        ps.println("400 " + uname);
        ps.flush();
    }


    //处理发送刷新列表请求之后服务端返回的数据
    public static void Refresh(String[] da) {
        if (da[0].equals("401")) {
            System.out.println("刷新成功");

        }

        //将所有资源信息加载到表格中
        for (int i = 1; i < da.length; i++) {
            Vector<String> Adi = new Vector<>();
            String[] da1 = da[i].split(",");
            for (int j = 0; j < da1.length; j++) {
                Adi.add(da1[j]);
            }
            data.add(Adi);
        }
        table.updateUI();
//        data.removeAllElements();
    }

    //请求资源函数
    public static void getSource(String srcname, String sendName, String sendIP) throws Exception {
        String receiveIP = InetAddress.getLocalHost().getHostAddress();
        ps.println("200 " + uname + " " + srcname + " " + sendName + " " + sendIP + " " + receiveIP);
        ps.flush();
        ts = new TransServer(uname);
        int result = ts.load();
        if (result == 1) {
            showCustomDialog(func, func, "文件获取成功");
        }
    }

    //申明资源函数
    private static void additem(String args) throws IOException {
        String info = "";
        String[] array = args.split("\\\\");
        filename = array[array.length - 1];

        String ip = InetAddress.getLocalHost().getHostAddress();
        String srcid = MD5.md5Encryption(args);
        info = uname + " " + filename + " " + srcid + " " + ip;
        ps.println("300 " + info);
        ps.flush();
    }

    //登出函数
    private static void leave(String args) throws IOException {
        ps.println("500 " + args);
        ps.flush();
    }


    //提示信息框函数
    public static void showCustomDialog(Frame owner, Component parentComponent, String info) {
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
        messageLabel = new JLabel(info);
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