package messagepack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author qihaodong
 */
public class EchoClient {

    public static void main(String[] args) {
        new EchoClient().connect();
    }

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 8888;
    private static final int NUM = 100;

    private void connect() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("frameDecoder"
                                , new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                        socketChannel.pipeline().addLast("msgpack decoder", new MsgPackDecoder());
                        socketChannel.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
                        socketChannel.pipeline().addLast("msgpack encoder", new MsgPackEncoder());
                        socketChannel.pipeline().addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                UserInfo[] infos = new UserInfo[NUM];
                                for (int i = 0; i < infos.length; i++) {
                                    infos[i] = new UserInfo(i + 1, "CLASS-" + (i + 1));
                                    ctx.write(infos[i]);
                                }
                                ctx.flush();
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                System.out.println("Client receive the msgpack message：" + msg);
                                ctx.write(msg);
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                ctx.flush();
                            }
                        });
                    }
                });
        ChannelFuture future = bootstrap.connect(ADDRESS, PORT);
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

}
