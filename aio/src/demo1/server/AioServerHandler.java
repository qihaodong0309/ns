package demo1.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author qihaodong
 */
public class AioServerHandler implements CompletionHandler<AsynchronousSocketChannel, AioServer> {

    @Override
    public void completed(AsynchronousSocketChannel result, AioServer attachment) {
        // 连接成功时再次调用 accept() 方法，表示等待其它客户端连接
        // 也是是说，每成功连接一个客户端，再异步调用该方法等待下一个客户端连接
        attachment.socketChannel.accept(attachment, this);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 此时通过 read() 方法进行异步读取
        // 其中三个参数分别表示，从 channel 读取数据的缓冲区、回调方法参数中的缓冲区，和具体的回调实现类
        result.read(buffer, buffer, new AioServerChannelHandler(result));
    }

    @Override
    public void failed(Throwable exc, AioServer attachment) {
        // 连接失败时，通过调用 countDown() 结束线程
        attachment.latch.countDown();
    }

}