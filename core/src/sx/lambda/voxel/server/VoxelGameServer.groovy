package sx.lambda.voxel.server

import groovy.transform.CompileStatic
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import sx.lambda.voxel.api.events.EventEarlyInit
import sx.lambda.voxel.api.events.EventWorldStart
import sx.lambda.voxel.server.dedicated.config.ServerConfig
import sx.lambda.voxel.server.net.VoxelGameServerHandler
import sx.lambda.voxel.world.IWorld
import sx.lambda.voxel.world.World
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.server.net.ConnectedClient

/**
 * Used for both local and remote servers
 * Local servers run the game when you are playing, remote servers allow you to play with others
 */
@CompileStatic
class VoxelGameServer {

    public static VoxelGameServer instance

    private ServerConfig config = new ServerConfig()
    private IWorld currentWorld
    private final Map<ChannelHandlerContext, ConnectedClient> clientMap;

    public VoxelGameServer() {
        currentWorld = new World(false, true)
        clientMap = new HashMap<ChannelHandlerContext, ConnectedClient>()
    }

    /**
     * Starts the server
     * @return Port listening on
     */
    public short start() {
        VoxelGameAPI.instance.registerBuiltinBlocks()
        VoxelGameAPI.instance.eventManager.push(new EventEarlyInit())

        EventLoopGroup bossGroup = new NioEventLoopGroup(1)
        EventLoopGroup workerGroup = new NioEventLoopGroup()

        try {
            ServerBootstrap b = new ServerBootstrap()
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline p = channel.pipeline()
                    p.addLast(
                            new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(this.getClass().getClassLoader())),
                            new VoxelGameServerHandler(VoxelGameServer.this)
                    )
                }
            })

            b.bind(config.port).sync().channel().closeFuture().sync();
        } catch(Exception e) {
            e.printStackTrace()
            bossGroup.shutdownGracefully()
            return -1;
        } finally {
            bossGroup.shutdownGracefully()
        }

        config.port
    }

    IWorld getWorld() { currentWorld }

    void addClient(ChannelHandlerContext ctx, ConnectedClient client) {
        this.clientMap.put(ctx, client);
    }

    ConnectedClient getClient(ChannelHandlerContext key) {
        return this.clientMap.get(key);
    }

    void rmClient(ChannelHandlerContext ctx) {
        this.clientMap.remove(ctx)
    }

    ServerConfig getConfig() {
        return this.config
    }

    Collection<ConnectedClient> getClientList() {
        return this.clientMap.values()
    }

    static void main(String[] args) {
        instance = new VoxelGameServer()
        VoxelGameAPI.instance.eventManager.push(new EventWorldStart())
        instance.start() 
    }

}
