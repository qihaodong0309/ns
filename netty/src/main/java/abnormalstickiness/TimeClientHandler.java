package abnormalstickiness;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author qihaodong
 */
public class TimeClientHandler extends ChannelHandlerAdapter {

    /**
     * 用来记录客户端发送的记录数
     */
    private int counter = 0;

    private byte[] bytes = "QUERY TIME ORDER\n".getBytes();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = null;
        for (int i = 0; i < 100; i++) {
            buf = Unpooled.buffer(bytes.length);
            buf.writeBytes(bytes);
            ctx.writeAndFlush(buf);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String message = new String(bytes, "UTF-8").substring(0, bytes.length - "\n".length());
        System.out.println("Time Client Receive message：[" + message + "] , and counter =  " + ++counter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
