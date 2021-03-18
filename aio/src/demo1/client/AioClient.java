package demo1.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * AIO Client 客户端
 *
 * @author qihaodong
 */
public class AioClient implements Runnable, CompletionHandler<Void, AioClient> {

    public static void main(String[] args) {
        new Thread(new AioClient()).start();
    }

    private static final String ADDRESS = "127.0.0.1";

    private static final int PORT = 8888;

    /**
     * AIO 客户端套接字
     */
    private AsynchronousSocketChannel channel;

    /**
     * 同步锁
     */
    private CountDownLatch latch;

    public AioClient() {
        try {
            // 初始化客户端套接字
            channel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // 初始化倒计时锁存
        latch = new CountDownLatch(1);
        // 客户端采用异步连接的方式，其中异步回调类就是本身，并且回调类方法参数也是本身
        channel.connect(new InetSocketAddress(ADDRESS, PORT), this, this);
        try {
            // 使当前线程阻塞，除非 latch 对象调用1次 countDown() 方法
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 代码通过 await() 方法时，表示客户端也该停止了
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, AioClient attachment) {
        // 此时表示异步连接成功，开始向服务端发送消息
        byte[] bytes = "QUERY TIME ORDER".getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                // 判断缓冲区中是否还存在未发送完的数据
                if (attachment.hasRemaining()) {
                    // 没发送完就继续发
                    channel.write(attachment, attachment, this);
                } else {
                    // 发送完毕后，异步等待服务端返回
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    channel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            // 此时表示服务端返回数据已读取完毕
                            attachment.flip();
                            byte[] message = new byte[attachment.remaining()];
                            attachment.get(message);
                            String body = null;
                            try {
                                body = new String(message, "UTF-8");
                                System.out.println("receive message from Server：" + body);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } finally {
                                latch.countDown();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            // 服务端返回数据读取失败
                            try {
                                channel.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                // 异步发送数据失败
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, AioClient attachment) {
        // 异步连接服务端失败
        exc.printStackTrace();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

}
