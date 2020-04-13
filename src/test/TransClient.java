package test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * 文件传输Client端<br>
 * 功能说明：
 *
 * @author 大智若愚的小懂
 * @version 1.0
 * @Date 2016年09月01日
 */
public class TransClient extends Socket {
    private static final int SERVER_PORT = 8888; // 服务端端口
    private String filename = "";
    private String usrname = "";
    private String path = "D:\\";

    private Socket client;

    private FileInputStream fis;

    private DataOutputStream dos;

    /**
     * 构造函数<br/>
     * 与服务器建立连接
     *
     * @throws Exception
     */
    public TransClient(String SERVER_IP, String filename, String usrname) throws Exception {
        super(SERVER_IP, SERVER_PORT);
        this.filename = filename;
        this.client = this;
        this.usrname = usrname;
        System.out.println("Client[port:" + client.getLocalPort() + "] 成功连接服务端");
    }

    /**
     * 向服务端传输文件
     *
     * @throws Exception
     */
    public int sendFile() throws Exception {
        try {
            File file = new File(path + usrname + "\\" + filename);
            System.out.println(path + usrname + "\\" + filename);
            if (file.exists()) {
                fis = new FileInputStream(file);
                dos = new DataOutputStream(client.getOutputStream());

                // 文件名和长度
                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();

                // 开始传输文件
                System.out.println("======== 开始传输文件 ========");
                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);
                    dos.flush();
                    progress += length;
                    System.out.print("| " + (100 * progress / file.length()) + "% |");
                }
                System.out.println();
                System.out.println("======== 文件传输成功 ========");

                return 1;
            } else {
                System.out.println("======== 文件不存在 ========");
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                fis.close();
            if (dos != null)
                dos.close();
            client.close();
        }
        return 0;
    }
}