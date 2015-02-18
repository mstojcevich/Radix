package sx.lambda.mstojcevich.voxel.client.net

import groovy.transform.CompileStatic
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder

/**
 * Connection to a voxel game server
 */
@CompileStatic
class ClientConnection {

    private static final short DEFAULT_PORT = 31173
    private static final String DEFAULT_HOSTNAME = "127.0.0.1"

    private final String hostname
    private final short port

    /**
     *
     * @param hostname
     * @param port
     */
    public ClientConnection(String hostname, short port) {
        this.hostname = hostname
        this.port = port
    }

    public ClientConnection(String hostname) {
        this(hostname, DEFAULT_PORT)
    }

    public ClientConnection() {
        this(DEFAULT_HOSTNAME)
    }

    void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup()
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder())
                    ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(this.getClass().getClassLoader())))
                    ch.pipeline().addLast(new VoxelGameClientHandler())
                }
            })

            b.connect(hostname, port).sync().channel().closeFuture().sync()
        } catch(Exception e) {
            group.shutdownGracefully()
            throw e
        } finally {
            group.shutdownGracefully()
        }
    }

    /**
     * Starts a tester client that nothing is listening at
     * You probably shouldn't use this unless you're testing
     */
    public static void main(String[] args) {
        new ClientConnection().start()
    }

}
