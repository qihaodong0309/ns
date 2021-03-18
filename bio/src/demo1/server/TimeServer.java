package demo1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 模拟传统 BIO 模型
 * 服务端使用时间服务器
 *
 * @author qihaodong
 */
public class TimeServer {

    private final static int PORT = 8081;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("This Time-Server is start in port:" + PORT);
            Socket socket = null;
            while (true) {
                socket = serverSocket.accept();
                System.out.println("Time-Client connect success");
                new Thread(new TimeServerHandler(socket)).start();
            }
        } finally {
            if (serverSocket != null) {
                System.out.println("This Time-Server will be soon closed");
                serverSocket.close();
                serverSocket = null;
            }
        }
    }

}
