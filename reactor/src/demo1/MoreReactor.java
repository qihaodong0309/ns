package demo1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @author qihaodong
 */
public class MoreReactor implements Runnable {

    public static void main(String[] args) {
        new Thread(new MoreReactor()).start();
    }

    private static final int PORT = 8888;

    /**
     * NIO 服务端套接字
     */
    private ServerSocketChannel socketChannel = null;

    /**
     * 两个 NIO 多路复用器
     */
    private Selector[] selectors = new Selector[2];

    private MoreReactor() {
        try {
            // 初始化多路复用器
            selectors[0] = Selector.open();
            selectors[1] = Selector.open();
            // 初始化 NIO 服务端套接字
            socketChannel = ServerSocketChannel.open();
            // 服务端套接字绑定要监听的端口
            socketChannel.socket().bind(new InetSocketAddress(PORT));
            // 设置套接字为非阻塞类型
            socketChannel.configureBlocking(false);
            // 注册服务端套接字的请求就绪事件到多路复用器
            SelectionKey key = socketChannel.register(selectors[0], SelectionKey.OP_ACCEPT);
            // 创建 Acceptor 对象，并将该对象附加到选择键，后续可以通过 attachment() 方法获取该附加对象
            key.attach(new Acceptor());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // 只要线程没停止，无限轮询多路复用器
        while (!Thread.interrupted()) {
            for (Selector selector : selectors) {
                try {
                    // 多路复用器阻塞等待客户端连接
                    selector.select(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 返回就绪需要处理的选择键
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    // 分发就绪的选择键到不同的 Handler
                    dispatch(key);
                    // 处理过的选择键从迭代器中删除
                    iterator.remove();
                }
            }
        }
    }

    private void dispatch(SelectionKey key) {
        // 从选择键中获取附加对象，一般为线程对象
        Runnable runnable = (Runnable) key.attachment();
        // 如果选择键附加属性不为空，就运行该线程
        if (runnable != null) {
            runnable.run();
        }
    }

    private class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                // 完成三次握手，建立数据链路
                SocketChannel channel = socketChannel.accept();
                if (channel != null) {
                    // 获取随机值，将该通连接注册到随机某一个多路复用器上
                    int random = new Random().nextInt(2);
                    // 创建新 Handler 处理已经连接的数据链路
                    new Handler(selectors[random], channel);
                }
            } catch (IOException e) {
                // 说明数据链路建立失败
                e.printStackTrace();
            }
        }
    }

}

