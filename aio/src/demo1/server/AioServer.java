package demo1.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * AIO Server 服务端
 *
 * @author qihaodong
 */
public class AioServer implements Runnable {

    public static void main(String[] args) {
        new Thread(new AioServer()).start();
    }

    private static final int PORT = 8888;

    /**
     * AIO 服务端套接字
     */
    AsynchronousServerSocketChannel socketChannel = null;

    /**
     * 同步锁
     */
    CountDownLatch latch;

    public AioServer() {
        try {
            // 创建服务端套接字
            socketChannel = AsynchronousServerSocketChannel.open();
            // 服务端绑定端口
            socketChannel.bind(new InetSocketAddress(PORT));
            System.out.println("AIO server start in port : " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // 初始化倒计时锁存
        latch = new CountDownLatch(1);
        // 接受请求并处理方法
        doAccept();
        try {
            // 使当前线程阻塞，除非 latch 对象调用1次 countDown() 方法
            // 这里必须先阻塞着，不然 doAccept() 方法中异步调用没返回前，由于没有后续操作，服务端会停止
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doAccept() {
        // 异步接受客户端连接，我们通过 CompletionHandler 类型的 handler 处理连接成功的事件
        // 这里 this 表示异步回调方法中的参数
        socketChannel.accept(this, new AioServerHandler());
    }

}