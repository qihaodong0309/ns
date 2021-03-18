package abnormalstickiness;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author qihaodong
 */
public class TimeServer {

    public static void main(String[] args) {
        // 启动服务端，调用初始化方法
        new TimeServer().init();
    }

    private static final int PORT = 8888;

    private void init() {
        // 创建两个 NIO 线程组，这两个线程组就类似多 Reactor 模式中的 Reactor
        // group 线程组主要用于处理客户端连接
        EventLoopGroup group = new NioEventLoopGroup();
        // workGroup 线程组主要用于管道的读写操作
        EventLoopGroup workGroup = new NioEventLoopGroup();
        // bootstrap 是 Netty 用于启动 NIO 服务端的辅助类，主要用来简化代码
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 将两个线程组作为参数传递，初始化 NIO 启动辅助类
        bootstrap.group(group, workGroup)
                // 设置 Channel 类型为 NioServerSocketChannel
                .channel(NioServerSocketChannel.class)
                // 设置最大连接数为 1024
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 绑定 I/O 事件处理类
                .childHandler(new ChildChannelHandler());
        try {
            // 调用 bind(PORT) 方法绑定监听端口
            // 调用 sync() 方法等待绑定操作完成，操作完成后返回 ChannelFuture 对象
            // ChannelFuture 对象就类似 Future，主要用于异步操作的回调通知
            ChannelFuture f = bootstrap.bind(PORT).sync();
            // 调用 f.channel().closeFuture().sync() 阻塞等待服务端数据链路中断
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 回收资源
            group.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            // 创建 Channel 成功后，在初始化时创建 TimeServerHandler 处理器，并让它绑定 pipeline，处理网络 IO 请求
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

}
