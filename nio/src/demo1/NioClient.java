package demo1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author qihaodong
 */
public class NioClient implements Runnable {

    public static void main(String[] args) {
        // 启动 NIO 客户端
        new Thread(new NioClient()).start();
    }

    /**
     * 用来记录连接的地址
     */
    private static final String ADDRESS = "127.0.0.1";

    /**
     * 用来记录连接的端口
     */
    private static final int PORT = 8888;

    /**
     * 用来标识服务端是否停止
     */
    private boolean stop = false;

    /**
     * 声明多路复用器对象
     */
    Selector selector = null;

    /**
     * 客户端套接字
     */
    SocketChannel socketChannel = null;

    private NioClient() {
        try {
            // 初始化资源
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            // 初始化资源失败则关闭程序
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            doConnect();
            while (!stop) {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    handleInput(key);
                }
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端连接服务器方法
     *
     * @throws IOException
     */
    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(ADDRESS, PORT))) {
            // 连接成功,将读就绪事件注册监听到管道
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            // 连接失败，可能是由于客户端还没有收到服务器的 TCP 连接报文
            // 将连接就绪事件注册监听到管道
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    /**
     * 处理就绪 Channel
     *
     * @param key
     */
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel channel = (SocketChannel) key.channel();
            // 判断是否连接就绪事件
            if (key.isConnectable()) {
                // 判断是否连接成功，此时可能收到服务端回复的 TCP 连接报文
                if (channel.finishConnect()) {
                    // 连接成功，将管道的读就绪事件注册到多路复用器
                    channel.register(selector, SelectionKey.OP_READ);
                    doWrite(socketChannel);
                } else {
                    // 连接失败，断开客户端程序
                    System.exit(1);
                }
            }
            if (key.isReadable()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int length = socketChannel.read(buffer);
                if (length > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    String message = new String(bytes, "UTF-8");
                    System.out.println("Received form Server：" + message);
                    // 一通完整的交互已完成，关闭客户端
                    this.stop = true;
                } else if (length < 0) {
                    key.channel();
                    socketChannel.close();
                } else {

                }
            }
        }
    }

    /**
     * 客户端发送消息方法
     *
     * @param socketChannel
     */
    private void doWrite(SocketChannel socketChannel) throws IOException {
        byte[] bytes = "QUERY TIME ORDER".getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        socketChannel.write(buffer);
        if (!buffer.hasRemaining()) {
            System.out.println("Send Message to Server Success");
        }
    }

}
