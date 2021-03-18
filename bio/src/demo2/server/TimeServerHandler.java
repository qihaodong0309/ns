package demo2.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;

/**
 * 时间服务器客户端处理类
 *
 * @author qihaodong
 */
public class TimeServerHandler implements Runnable {

    private Socket socket = null;

    public TimeServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        PrintStream writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            writer = new PrintStream(this.socket.getOutputStream(), true);
            String currentTime = null;
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                System.out.println("This Time-Server receive message :" + temp);
                currentTime = temp.equalsIgnoreCase("query time") ?
                        new Date(System.currentTimeMillis()).toString() : "I don't Know";
                writer.println(currentTime);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                    this.socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
