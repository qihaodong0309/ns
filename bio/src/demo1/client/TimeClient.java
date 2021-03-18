package demo1.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 时间模拟程序客户端
 *
 * @author qihaodong
 */
public class TimeClient {

    private final static int PORT = 8081;

    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader reader = null;
        PrintStream writer = null;
        try {
            socket = new Socket("127.0.0.1", PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintStream(socket.getOutputStream());
            writer.println("Query Time");
            System.out.println("Send message to Server Succeed");
            String response = reader.readLine();
            System.out.println("Received message from server：" + response);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
