package abnormalstickiness;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
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

}
