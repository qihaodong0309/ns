package timeproject;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Netty 客户端
 *
 * @author qihaodong
 */
public class TimeClient {

    public static void main(String[] args) {
        // 启动客户端，调用初始化方法
        new TimeClient().connect();
    }

    private static final String ADDRESS = "127.0.0.1";

    private static final int PORT = 8888;

    private void connect() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                // 绑定 Channel 类型为 NioSocketChannel
                .channel(NioSocketChannel.class)
                // 禁止 Nagle 算法，该算法为了减少 TCP 包的数量，会将较小的包组合成大包发送
                // 在部分场景下，由于 TCP 延迟，该算法可能导致连续发送两个请求包
                .option(ChannelOption.TCP_NODELAY, true)
                // 这里使用匿名内部类绑定处理器
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // NioSocketChannel 创建成功初始化时，将 TimeClientHandler 绑定到 pipeline，用于处理网络 IO
                        socketChannel.pipeline().addLast(new TimeClientHandler());
                    }
                });
        // 发起异步连接操作
        ChannelFuture future = bootstrap.connect(ADDRESS, PORT);
        // 阻塞等待客户端关闭链路
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private class TimeClientHandler extends ChannelHandlerAdapter {

        private final ByteBuf buf;

        private TimeClientHandler() {
            byte[] bytes = "QUERY TIME ORDER".getBytes();
            buf = Unpooled.buffer(bytes.length);
            buf.writeBytes(bytes);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(buf);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            String message = new String(bytes, "UTF-8");
            System.out.println("Client receive message：" + message);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

}
