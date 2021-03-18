package abnormalstickiness;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * @author qihaodong
 */
public class TimeServerHandler extends ChannelHandlerAdapter {

    /**
     * 用来记录收到客户端消息的次数
     */
    private int counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String body = new String(bytes, "UTF-8").substring(0, bytes.length - "\n".length());
        System.out.println("Time Server Receive message：[" + body + "] , and counter =  " + ++counter);
        String result = ("QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString() : "I don't Know") + "\n";
        ByteBuf resp = Unpooled.copiedBuffer(result.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
