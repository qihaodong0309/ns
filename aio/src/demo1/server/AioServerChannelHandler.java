package demo1.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * @author qihaodong
 */
public class AioServerChannelHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel channel;

    public AioServerChannelHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        // 将缓冲区切换为读模式
        attachment.flip();
        // 获取缓冲区可读的数据长度
        byte[] bytes = new byte[attachment.remaining()];
        // 将缓冲区中可读数据读取到字节数组
        attachment.get(bytes);
        try {
            // 解码消息内容
            String message = new String(bytes, "UTF-8");
            System.out.println("This time server receive message: " + message);
            // 根据消息内容设置返回值
            String resultMessage = message.equalsIgnoreCase("QUERY TIME ORDER")
                    ? new Date(System.currentTimeMillis()).toString() : "I don't know";
            // 异步返回数据
            doWrite(resultMessage);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void doWrite(String message) {
        if (message != null && message.trim().length() > 0) {
            byte[] bytes = message.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            // 将要返回给客户端的数据写入缓冲区
            buffer.put(bytes);
            // 从写模式切换到读模式，确定范围
            buffer.flip();
            // 异步发送数据给客户端
            channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    // 判断缓冲区中是否残留还未发送的数据
                    if (attachment.hasRemaining()) {
                        // 继续发送
                        channel.write(attachment, attachment, this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    // 异步发送数据给客户端失败
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        // 异步读数据到缓冲区失败
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}