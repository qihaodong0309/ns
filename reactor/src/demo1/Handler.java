package demo1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author qihaodong
 */
public class Handler implements Runnable {

    /**
     * 用来标记当前数据链路状态
     */
    static final int READING = 0, SENDING = 1;
    /**
     * 默认状态为读取数据状态
     */
    int state = READING;

    /**
     * 多路复用器对象
     */
    final SocketChannel channel;

    /**
     * 选择键对象
     */
    final SelectionKey selectionKey;

    /**
     * 读数据缓冲池
     */
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    /**
     * 写数据缓冲池
     */
    ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    /**
     * 此时两个参数分别表示：服务端多路复用器、客户端新建立的数据链路
     *
     * @param selector
     * @param socketChannel
     * @throws IOException
     */
    protected Handler(Selector selector, SocketChannel socketChannel) throws IOException {
        channel = socketChannel;
        // 设置新建立数据链路为非阻塞状态
        channel.configureBlocking(false);
        // 尝试注册一下，没有实际用处
        // 使用0注册选择键不会报错，但没有实际用处，
        // 本人理解可能是为了试错，判断该 channel 能否注册就绪事件到多路复用器
        selectionKey = channel.register(selector, 0);
        // 将当前类对象作为附加属性添加到选择键
        selectionKey.attach(this);
        // 将读就绪方法注册到多路复用器
        selectionKey.interestOps(SelectionKey.OP_READ);
        // 通过该方法唤醒阻塞在 select() 上的线程，使被唤醒线程即时去处理注册多路复用器等任务
        // 本示例为单线程，无实际用处，可以删除
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                read();
            } else if (state == SENDING) {
                send();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read() throws IOException {
        int length = channel.read(readBuffer);
        System.out.println("数据已读取完毕");
        state = SENDING;
        // 将写就绪事件注册到多路复用器
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void send() throws IOException {
        channel.write(writeBuffer);
        if (!writeBuffer.hasRemaining()) {
            System.out.println("数据已写完毕");
            selectionKey.cancel();
            // 关闭该选择键
            System.out.println("selectionKey关闭了");
        }
    }

}