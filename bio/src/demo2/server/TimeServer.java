package demo2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 通过线程池和等待队列实现优化
 *
 * @author qihaodong
 */
public class TimeServer {

    private final static int PORT = 8088;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            TimeServerHandlerExecutePool executePool = new TimeServerHandlerExecutePool(50, 1000);
            System.out.println("This Time-Server is start in port:" + PORT);
            Socket socket = null;
            while (true) {
                socket = serverSocket.accept();
                executePool.execute(new TimeServerHandler(socket));
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
