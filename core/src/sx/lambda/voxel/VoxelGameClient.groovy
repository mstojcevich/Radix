package sx.lambda.voxel

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.TextureData
import com.badlogic.gdx.utils.BufferUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import pw.oxcafebabe.marcusant.eventbus.Priority
import sx.lambda.voxel.api.events.EventEarlyInit
import sx.lambda.voxel.api.events.EventWorldStart
import sx.lambda.voxel.block.Block
import sx.lambda.voxel.client.gui.GuiScreen
import sx.lambda.voxel.client.gui.screens.IngameHUD
import sx.lambda.voxel.client.gui.screens.MainMenu
import sx.lambda.voxel.client.gui.transition.SlideUpAnimation
import sx.lambda.voxel.entity.EntityRotation
import sx.lambda.voxel.entity.player.Player
import sx.lambda.voxel.net.packet.client.PacketLeaving
import sx.lambda.voxel.render.NotInitializedException
import sx.lambda.voxel.render.game.GameRenderer
import sx.lambda.voxel.settings.SettingsManager
import sx.lambda.voxel.shader.GuiShader
import sx.lambda.voxel.shader.PostProcessShader
import sx.lambda.voxel.shader.WorldShader
import sx.lambda.voxel.tasks.EntityUpdater
import sx.lambda.voxel.tasks.InputHandler
import sx.lambda.voxel.tasks.LightUpdater
import sx.lambda.voxel.tasks.MovementHandler
import sx.lambda.voxel.tasks.RepeatedTask
import sx.lambda.voxel.tasks.RotationHandler
import sx.lambda.voxel.tasks.WorldLoader
import sx.lambda.voxel.texture.TextureManager
import sx.lambda.voxel.util.PlotCell3f
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.util.gl.ShaderManager
import sx.lambda.voxel.util.gl.ShaderProgram
import sx.lambda.voxel.world.IWorld
import sx.lambda.voxel.world.World
import sx.lambda.voxel.world.chunk.IChunk
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.client.gui.transition.TransitionAnimation
import sx.lambda.voxel.client.net.ClientConnection
import sx.lambda.voxel.entity.EntityPosition
import sx.lambda.voxel.render.Renderer
import pw.oxcafebabe.marcusant.eventbus.EventListener

import com.badlogic.gdx.Graphics.DisplayMode

import javax.imageio.ImageIO
import javax.swing.*
import javax.vecmath.Vector3f
import java.awt.*
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedDeque

import static GL20.*

@CompileStatic
public class VoxelGameClient extends ApplicationAdapter {

    // TODO CALCULATE FRUSTUM ON WORLD JOIN

    public static final boolean DEBUG = false

    private static VoxelGameClient theGame

    private SettingsManager settingsManager

    public static final String GAME_TITLE = "VoxelTest"

    private IWorld world

    private Player player

    private boolean done

    private synchronized Vec3i selectedBlock, selectedNextPlace

    private Queue<Runnable> glQueue = new ConcurrentLinkedDeque<>()

    private MainMenu mainMenu
    private IngameHUD hud
    private GuiScreen currentScreen

    private int fps
    public int chunkRenderTimes = 0 // TODO move to World
    public int numChunkRenders = 0 // TODO move to World

    private TextureManager textureManager = new TextureManager()
    private ShaderManager shaderManager = new ShaderManager()

    private Renderer renderer

    private WorldShader defaultShader
    private PostProcessShader postProcessShader
    private GuiShader guiShader

    private TransitionAnimation transitionAnimation

    private boolean remote

    private ChannelHandlerContext serverChanCtx;

    private GameRenderer gameRenderer

    private Texture blockTextureAtlas

    private PerspectiveCamera camera
    private OrthographicCamera hudCamera

    private RepeatedTask[] handlers = [
            new WorldLoader(this),
            new InputHandler(this),
            new MovementHandler(this),
            new RotationHandler(this),
            new EntityUpdater(this),
            new LightUpdater(this)
    ]

    @Override
    public void create() {
        theGame = this

        VoxelGameAPI.instance.registerBuiltinBlocks()
        VoxelGameAPI.instance.eventManager.register(this)
        VoxelGameAPI.instance.eventManager.push(new EventEarlyInit())

        settingsManager = new SettingsManager()
        this.setupOGL();

        gameRenderer = new GameRenderer(this)
        hud = new IngameHUD()
        mainMenu = new MainMenu()
        setCurrentScreen(mainMenu)

        currentScreen = mainMenu

        this.startHandlers()

        Gdx.input.setInputProcessor(new VoxelGameGdxInputHandler())
    }

    private void startHandlers() {
        for(RepeatedTask r : handlers) {
            new Thread(r, r.getIdentifier()).start()
        }
    }

    private void setupOGL() {
        Gdx.gl.glEnable GL_TEXTURE_2D
        Gdx.gl.glClearColor(0.2f, 0.4f, 1, 0) //Set default color
        Gdx.gl.glEnable GL_DEPTH_TEST //Enable depth visibility check
        Gdx.gl.glDepthFunc GL_LEQUAL //How to test depth (less than or equal)

        camera = new PerspectiveCamera()
        camera.position.set(10f, 150f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
        hudCamera = new OrthographicCamera()

        defaultShader = createShader("default", WorldShader.class)
        postProcessShader = createShader("post-process", PostProcessShader.class)
        guiShader = createShader("gui", GuiShader.class)

        getShaderManager().setShader(defaultShader)
        //defaultShader.setUniformMatrix("u_projectionViewMatrix", camera.combined)
    }

    @Override
    public void dispose() {
        super.dispose()
        done = true
        if (isRemote() && this.serverChanCtx != null) {
            this.serverChanCtx.writeAndFlush(new PacketLeaving("Game closed"))
            this.serverChanCtx.disconnect()
        }
    }

    @Override
    public void render() {
        prepareNewFrame()

        runQueuedOGL()

        if(renderer != null) {
            synchronized (renderer) {
                renderer.render()
            }
        }

        //2D starts here
        prepare2D()

        Gdx.gl.glEnable(GL_BLEND)

        if(renderer != null) {
            synchronized (renderer) {
                renderer.draw2d()
            }
        }

        synchronized (currentScreen) {
            currentScreen.render(true)
            if(transitionAnimation != null) {
                transitionAnimation.render()
                if(transitionAnimation.isFinished()) {
                    transitionAnimation.finish()
                    transitionAnimation = null
                }
            }
        }

        Thread.yield()
    }

    private void runQueuedOGL() {
        Runnable currentRunnable
        while ((currentRunnable = glQueue.poll()) != null) {
            currentRunnable.run()
        }
    }

    private void prepare2D() {
        hudCamera.setToOrtho(false, Gdx.graphics.width, Gdx.graphics.height)
        hudCamera.update()
        guiShader.setUniformMatrix("u_projectionViewMatrix", hudCamera.combined)
        Gdx.gl.glDisable GL_DEPTH_TEST
        worldShader.disableLighting()
    }

    private void prepareNewFrame() {
        if(!settingsManager.visualSettings.postProcessEnabled) {
            Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        }

        float camNear = 0.1f
        float camFar = 0f
        if(world != null) {
            camFar = settingsManager.visualSettings.viewDistance * world.chunkSize
        }
        camera.near = camNear
        camera.far = camFar
        camera.update()

        getTextureManager().bindTexture(-1)

        worldShader.enableLighting()

        worldShader.updateAnimTime();
    }

    public void updateSelectedBlock() {
        PlotCell3f plotter = new PlotCell3f(0, 0, 0, 1, 1, 1)
        float x = player.getPosition().getX()
        float y = player.getPosition().getY() + player.getEyeHeight()
        float z = player.getPosition().getZ()
        float pitch = player.getRotation().getPitch()
        float yaw = player.getRotation().getYaw()
        float reach = player.getReach()

        float deltaX = (float)(Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)))
        float deltaY = (float)(Math.sin(Math.toRadians(pitch)))
        float deltaZ = (float)(-Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)))

        plotter.plot(new Vector3f(x, y, z), new Vector3f(deltaX, deltaY, deltaZ), (int) Math.ceil(reach * reach))
        Vec3i last
        while (plotter.next()) {
            Vec3i v = plotter.get()
            Vec3i bp = new Vec3i(v.x, v.y, v.z)
            IChunk theChunk = world.getChunkAtPosition(bp)
            if (theChunk != null) {
                if (theChunk.getBlockAtPosition(bp) != null) {
                    selectedBlock = bp
                    if (last != null) {
                        if (theChunk.getBlockAtPosition(last) == null) {
                            selectedNextPlace = last
                        }
                    }
                    plotter.end()
                    return
                }
                last = bp;
            }
        }
        selectedNextPlace = null
        selectedBlock = null
    }

    public void addToGLQueue(Runnable runnable) {
        glQueue.add(runnable)
    }

    public static VoxelGameClient getInstance() {
        theGame
    }

    public IWorld getWorld() { world }

    public Player getPlayer() { player }

    public TextureManager getTextureManager() { textureManager }

    public ShaderManager getShaderManager() { shaderManager }

    public boolean isDone() { done }

    public SettingsManager getSettingsManager() { settingsManager }

    public Vec3i getSelectedBlock() { selectedBlock }

    public Vec3i getNextPlacePos() { selectedNextPlace }

    public void startShutdown() {
        done = true
    }

    private void setRenderer(Renderer renderer) {
        if(renderer == null) {
            if(this.renderer != null) {
                synchronized (this.renderer) {
                    this.renderer.cleanup()
                    this.renderer = null
                }
            } else {
                this.renderer = null
            }
        } else if(this.renderer == null) {
            this.renderer = renderer
            this.renderer.init()
        } else if(this.renderer != renderer) {
            synchronized (this.renderer) {
                this.renderer.cleanup()
                this.renderer = renderer
                this.renderer.init()
            }
        }
    }

    public <T extends ShaderProgram> T createShader(String shaderName, Class<T> type) {
        String nameNoExt = "shaders/$shaderName/$shaderName"
        String vertex = Gdx.files.internal(nameNoExt + ".vert.glsl").readString()
        String fragment = Gdx.files.internal(nameNoExt + ".frag.glsl").readString()

        Object[] args = [vertex, fragment]
        ShaderProgram program = (ShaderProgram)type.newInstance(args);

        if(!program.getLog().isEmpty()) {
            System.err.println(program.getLog())
            return null
        }

        return (T)program
    }

    public boolean isRemote() {
        return remote
    }

    public ChannelHandlerContext getServerChanCtx() { serverChanCtx }

    public void setServerChanCtx(ChannelHandlerContext ctx) { serverChanCtx = ctx }

    public void handleCriticalException(Exception ex) {
        ex.printStackTrace()
        this.done = true
        Gdx.input.setCursorCatched false
        JOptionPane.showMessageDialog(null, "$GAME_TITLE crashed. $ex", "$GAME_TITLE crashed", JOptionPane.ERROR_MESSAGE)
    }

    public GuiScreen getCurrentScreen() { currentScreen }

    public void enablePostProcessShader() {
        if(settingsManager.visualSettings.postProcessEnabled) {
            shaderManager.setShader(postProcessShader)
            hudCamera.setToOrtho(false, Gdx.graphics.width, Gdx.graphics.height)
            hudCamera.update()
            postProcessShader.setUniformMatrix("u_projectionViewMatrix", hudCamera.combined)
        }
    }

    public void enableDefaultShader() {
        shaderManager.setShader(defaultShader)
    }

    public void enableGuiShader() {
        shaderManager.setShader(guiShader)
    }

    public GuiShader getGuiShader() { guiShader }

    public PostProcessShader getPostProcessShader() { postProcessShader }

    public int getFps() { fps }

    public IngameHUD getHud() { this.hud }

    public GameRenderer getGameRenderer() { this.gameRenderer }

    public void enterRemoteWorld(String hostname, short port) {
        enterWorld(new World(true, false), true)

        new Thread("Client Connection") {
            @Override
            public void run() {
                new ClientConnection(hostname, port).start()
            }
        }.start()
    }

    public void enterLocalWorld(IWorld world) {
        enterWorld(world, false)
    }

    public void exitWorld() {

        addToGLQueue(new Runnable() {
            @Override
            void run() {
                setCurrentScreen(mainMenu)
                setRenderer(null)
                world.cleanup()
                if(remote) {
                    if(serverChanCtx != null) {
                        serverChanCtx.writeAndFlush(new PacketLeaving("Leaving"))
                        serverChanCtx.disconnect()
                        serverChanCtx = null
                    }
                }
                world = null
                remote = false
                player = null
            }
        })

    }

    private void enterWorld(IWorld world, boolean remote) {
        this.world = world
        this.remote = remote
        player = new Player(new EntityPosition(0, 256, 0), new EntityRotation(0, 0))
        glQueue.add(new Runnable() { // A lot of the init methods require GL context
            @Override
            @CompileDynamic // Using VoxelGame.this causes the static compile checker to freak out
            void run() {
                setRenderer(gameRenderer)
                player.init()
                world.addEntity(player)
                transitionAnimation = new SlideUpAnimation(currentScreen, 1000)
                transitionAnimation.init()
                setCurrentScreen(hud)

                if(!remote) {
                    world.loadChunks(new EntityPosition(0, 0, 0), getSettingsManager().getVisualSettings().getViewDistance())
                }

                VoxelGameAPI.instance.eventManager.push(new EventWorldStart())
                // Delays are somewhere in this function. Above here.
            }
        })
    }

    public void setCurrentScreen(GuiScreen screen) {
        if(currentScreen == null) {
            currentScreen = screen
            screen.init()
        } else if(currentScreen != screen) {
            synchronized (currentScreen) {
                currentScreen.finish()
                currentScreen = screen
                screen.init()
            }
        }
        if(screen == hud) {
            Gdx.input.setCursorCatched true
        } else {
            Gdx.input.setCursorCatched false
        }
    }

    public WorldShader getWorldShader() { defaultShader }

    public Texture getBlockTextureAtlas() throws NotInitializedException {
        if(blockTextureAtlas == null)throw new NotInitializedException()
        return blockTextureAtlas
    }

    @EventListener(Priority.LAST)
    public void onBlockRegister(EventEarlyInit event) {
        // Create a texture atlas for all of the blocks
        BufferedImage bi = new BufferedImage(1024, 1024, BufferedImage.TYPE_4BYTE_ABGR)
        Graphics2D g = bi.createGraphics()
        final int BLOCK_TEX_SIZE = 32
        for(Block b : VoxelGameAPI.instance.blocks) {
            int x = b.ID*BLOCK_TEX_SIZE % bi.getWidth()
            int y = b.ID*BLOCK_TEX_SIZE / bi.getWidth() as int
            InputStream texIS = b.textureLocation.read()
            BufferedImage texImg = ImageIO.read(texIS)
            texIS.close()
            g.drawImage(texImg, x, y, BLOCK_TEX_SIZE, BLOCK_TEX_SIZE, null)
        }
        g.dispose()
        addToGLQueue(new Runnable() {
            @Override
            void run() {
                ByteBuffer imageBuffer = BufferUtils.newByteBuffer(4 * bi.getWidth() * bi.getHeight());
                byte[] imageInByte = (byte[])bi.getRaster().getDataElements(0, 0, bi.getWidth(), bi.getHeight(), null);
                imageBuffer.put(imageInByte);
                imageBuffer.flip();

                int colorMode = GL_RGBA;

                blockTextureAtlas = new Texture(bi.getWidth(), bi.getHeight(), Pixmap.Format.RGBA8888)
                textureManager.bindTexture(blockTextureAtlas.getTextureObjectHandle())
                Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, colorMode, bi.getWidth(), bi.getHeight(), 0, colorMode, GL_UNSIGNED_BYTE, imageBuffer)
                Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            }
        })
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }
    
    //TODO move frustum calc, light pos, etc into GameRenderer

}
