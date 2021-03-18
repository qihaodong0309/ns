package demo1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author qihaodong
 */
public class NioServer implements Runnable {

    public static void main(String[] args) throws IOException {
        // 启动 NIO 服务器
        new Thread(new NioServer()).start();
    }

    /**
     * 用来记录服务端监听端口
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
     * 声明服务端套接字
     */
    ServerSocketChannel serverSocketChannel = null;

    private NioServer() {
        try {
            // 创建多路复用器对象
            selector = Selector.open();
            // 创建 NIO 服务端套接字
            serverSocketChannel = ServerSocketChannel.open();
            // 设置模式为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 绑定监听端口，设置请求最大数量
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT), 1024);
            // 将套接字注册到多路复用器上，监听 ACCEPT 事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            // 资源初始化失败（可能是由于端口被占用），停止程序
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            // 多路复用器在 run() 方法的无限循环体中轮询就绪的 Key
            while (!stop) {
                // 这里1000表示休眠时间，每隔1s，多路复用器被唤醒一次
                selector.select(1000);
                // 该方法返回应该被处理的 SelectionKey 集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                // 使用迭代器遍历 SelectionKey 集合
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(key);
                    } catch (IOException e) {
                        // 如果处理就绪事件时出现问题（可能由于该管道已停止）
                        // 关闭选择键，并关闭该键对应的管道
                        if (key != null) {
                            key.channel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            }
            // 程序执行到这里说明多路复用器已跳出循环，关闭多路复用器，释放资源
            if (selector != null) {
                selector.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理就绪的 Channel
     *
     * @param key
     * @throws IOException
     */
    private void handleInput(SelectionKey key) throws IOException {
        // 判断该选择键是否有效
        if (key.isValid()) {
            // 判断是否连接就绪
            if (key.isAcceptable()) {
                // 获取服务端套接字
                ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
                // 完成三次握手，建立正式链路
                SocketChannel channel = socketChannel.accept();
                // 设置该通管道为非阻塞
                channel.configureBlocking(false);
                // 注册该管道的读就绪事件到多路复用器
                channel.register(selector, SelectionKey.OP_READ);
            }
            // 判断是否读就绪
            if (key.isReadable()) {
                // 获取对应管道 Channel
                SocketChannel channel = (SocketChannel) key.channel();
                // 创建 ByteBuffer 缓冲区，默认设置大小为 1M
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // 此时通道是非阻塞的，因此需要对返回值判断
                int length = channel.read(buffer);
                if (length > 0) {
                    // 如果大于0，说明读取到字节，对字节进行编解码
                    // filp() 主要为了方便后期缓冲区读取操作，下文我们详细介绍
                    buffer.flip();
                    // 创建需要处理大小的字节数组
                    byte[] bytes = new byte[buffer.remaining()];
                    // 将缓冲区可读的字节数组复制到我们创建的字节数组中
                    buffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("This time server receive message：" + body);
                    String result = body.equalsIgnoreCase("QUERY TIME ORDER")
                            ? new Date(System.currentTimeMillis()).toString() : "I don't know";
                    // 将请求结果返回给客户端
                    doWrite(channel, result);
                } else if (length < 0) {
                    // 如果小于0，说明链路已关闭，需要关闭管道、释放资源
                    key.channel();
                    channel.close();
                } else {
                    // 如果等于0，属于正常情况，忽略
                }
            }
        }
    }

    /**
     * 向客户端写数据
     *
     * @param channel
     * @param message
     * @throws IOException
     */
    private void doWrite(SocketChannel channel, String message) throws IOException {
        if (message != null && message.trim().length() > 0) {
            byte[] bytes = message.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            channel.write(buffer);
        }
    }

}
