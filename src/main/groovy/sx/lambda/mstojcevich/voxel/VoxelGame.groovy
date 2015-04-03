package sx.lambda.mstojcevich.voxel

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import org.lwjgl.LWJGLException
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.util.glu.GLU

import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI
import sx.lambda.mstojcevich.voxel.api.events.EventWorldStart
import sx.lambda.mstojcevich.voxel.client.gui.GuiScreen
import sx.lambda.mstojcevich.voxel.client.gui.screens.IngameHUD
import sx.lambda.mstojcevich.voxel.client.gui.screens.MainMenu
import sx.lambda.mstojcevich.voxel.client.gui.transition.SlideUpAnimation
import sx.lambda.mstojcevich.voxel.client.gui.transition.TransitionAnimation
import sx.lambda.mstojcevich.voxel.entity.EntityRotation
import sx.lambda.mstojcevich.voxel.net.packet.client.PacketLeaving
import sx.lambda.mstojcevich.voxel.render.Renderer
import sx.lambda.mstojcevich.voxel.settings.SettingsManager
import sx.lambda.mstojcevich.voxel.shader.GuiShader
import sx.lambda.mstojcevich.voxel.shader.PostProcessShader
import sx.lambda.mstojcevich.voxel.shader.WorldShader
import sx.lambda.mstojcevich.voxel.tasks.EntityUpdater
import sx.lambda.mstojcevich.voxel.tasks.InputHandler
import sx.lambda.mstojcevich.voxel.tasks.LightUpdater
import sx.lambda.mstojcevich.voxel.tasks.MovementHandler
import sx.lambda.mstojcevich.voxel.tasks.RepeatedTask
import sx.lambda.mstojcevich.voxel.world.IWorld
import sx.lambda.mstojcevich.voxel.world.World
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk
import sx.lambda.mstojcevich.voxel.client.net.ClientConnection
import sx.lambda.mstojcevich.voxel.entity.EntityPosition
import sx.lambda.mstojcevich.voxel.entity.player.Player
import sx.lambda.mstojcevich.voxel.render.game.GameRenderer
import sx.lambda.mstojcevich.voxel.tasks.RotationHandler
import sx.lambda.mstojcevich.voxel.tasks.WorldLoader
import sx.lambda.mstojcevich.voxel.texture.TextureManager
import sx.lambda.mstojcevich.voxel.util.PlotCell3f
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.util.gl.ShaderManager
import sx.lambda.mstojcevich.voxel.util.gl.ShaderProgram

import javax.swing.JOptionPane
import javax.vecmath.Vector3f
import java.nio.FloatBuffer
import java.util.concurrent.ConcurrentLinkedDeque

import static org.lwjgl.opengl.GL11.*

@CompileStatic
public class VoxelGame {

    // TODO CALCULATE FRUSTUM ON WORLD JOIN

    public static final boolean DEBUG = false

    private static VoxelGame theGame

    private SettingsManager settingsManager

    public static final String GAME_TITLE = "VoxelTest"

    private int renderedFrames = 0;

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

    private RepeatedTask[] handlers = [
            new WorldLoader(this),
            new InputHandler(this),
            new MovementHandler(this),
            new RotationHandler(this),
            new EntityUpdater(this),
            new LightUpdater(this)
    ]

    public static void main(String[] args) throws Exception {
        theGame = new VoxelGame();
        theGame.start();
    }

    private void start() throws LWJGLException {
        settingsManager = new SettingsManager()
        setupWindow();
        this.setupOGL();

        gameRenderer = new GameRenderer(this)
        hud = new IngameHUD()
        mainMenu = new MainMenu()
        setCurrentScreen(mainMenu)

        currentScreen = mainMenu

        this.startHandlers()
        this.run()
    }

    private void startHandlers() {
        for(RepeatedTask r : handlers) {
            new Thread(r, r.getIdentifier()).start()
        }
    }

    private void setupWindow() throws LWJGLException {
        Display.setFullscreen getSettingsManager().getVisualSettings().isFullscreen()

        Display.setTitle GAME_TITLE

        Display.setDisplayMode(new DisplayMode(
                settingsManager.visualSettings.windowWidth,
                settingsManager.visualSettings.windowHeight))
        Display.setResizable false

        Display.create()
    }

    private void setupOGL() {
        glEnable GL_TEXTURE_2D
        glShadeModel GL_SMOOTH
        glClearColor(0.2f, 0.4f, 1, 0) //Set default color
        glClearDepth 1 //Set default depth buffer depth
        glEnable GL_DEPTH_TEST //Enable depth visibility check
        glDepthFunc GL_LEQUAL //How to test depth (less than or equal)

        glMatrixMode GL_PROJECTION //Currently altering projection matrix
        glLoadIdentity()

        glMatrixMode GL_MODELVIEW //Currently altering modelview matrix
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)
        glEnable GL_CULL_FACE
        glFrontFace GL_CCW

        defaultShader = createShader("default", WorldShader.class)
        postProcessShader = createShader("post-process", PostProcessShader.class)
        guiShader = createShader("gui", GuiShader.class)

        getShaderManager().setShader(defaultShader)
    }

    private void run() {
        long startTime = System.currentTimeMillis()
        try {
            while (!Display.isCloseRequested() && !done) {
                render()

                Display.update()
                Display.sync getSettingsManager().getVisualSettings().getMaxFPS()

                if (renderedFrames % 100 == 0) {
                    fps = (int)(renderedFrames / ((System.currentTimeMillis() - startTime) / 1000))
                    startTime = System.currentTimeMillis()
                    renderedFrames = 0
                }

                Thread.yield()
            }
            done = true
            Display.destroy()
            if (isRemote() && this.serverChanCtx != null) {
                this.serverChanCtx.writeAndFlush(new PacketLeaving("Game closed"))
                this.serverChanCtx.disconnect()
            }
        } catch (Exception e) {
            handleCriticalException(e)
        }
    }

    private void render() {
        prepareNewFrame()

        runQueuedOGL()

        if(renderer != null) {
            synchronized (renderer) {
                glPushMatrix()
                renderer.render()
                glPopMatrix()
            }
        }

        //2D starts here
        glPushMatrix()
        glPushAttrib GL_ENABLE_BIT
        prepare2D()

        glEnable(GL_BLEND)

        if(renderer != null) {
            synchronized (renderer) {
                renderer.draw2d()
            }
        }

        synchronized (currentScreen) {
            enableGuiShader()
            currentScreen.render(true)
            if(transitionAnimation != null) {
                transitionAnimation.render()
                if(transitionAnimation.isFinished()) {
                    transitionAnimation.finish()
                    transitionAnimation = null
                }
            }
            enableDefaultShader()
        }

        glPopMatrix()
        glPopAttrib()

        renderedFrames++
        Thread.yield()
    }

    private void runQueuedOGL() {
        Runnable currentRunnable
        while ((currentRunnable = glQueue.poll()) != null) {
            currentRunnable.run()
        }
    }

    private void prepare2D() {
        glMatrixMode GL_PROJECTION
        glLoadIdentity()
        glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -1, 1)
        glMatrixMode GL_MODELVIEW
        glLoadIdentity()
        glColor4f(1, 1, 1, 1)
        glDisable GL_LIGHTING
        glDisable GL_DEPTH_TEST
        worldShader.disableLighting()
    }

    private void prepareNewFrame() {
        if(!settingsManager.visualSettings.postProcessEnabled) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        }
        glMatrixMode GL_PROJECTION //Currently altering projection matrix
        glLoadIdentity()

        float camNear = 0.1f
        float camFar = 0f
        if(world != null) {
            camFar = settingsManager.visualSettings.viewDistance * world.chunkSize
        }
        GLU.gluPerspective(100, (float) Display.getWidth() / Display.getHeight(), camNear, camFar)
        //Set up camera

        glMatrixMode GL_MODELVIEW //Currently altering modelview matrix
        glLoadIdentity()

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

    public static VoxelGame getInstance() {
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
        String nameNoExt = "/shaders/$shaderName/$shaderName"
        String vertex = this.getClass().getResourceAsStream(nameNoExt + ".vert.glsl").getText()
        String fragment = this.getClass().getResourceAsStream(nameNoExt + ".frag.glsl").getText()

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
        Mouse.setGrabbed false
        JOptionPane.showMessageDialog(null, "$GAME_TITLE crashed. $ex", "$GAME_TITLE crashed", JOptionPane.ERROR_MESSAGE)
    }

    public GuiScreen getCurrentScreen() { currentScreen }

    public void enablePostProcessShader() {
        if(settingsManager.visualSettings.postProcessEnabled) {
            shaderManager.setShader(postProcessShader)
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

    private void setCurrentScreen(GuiScreen screen) {
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
            Mouse.setGrabbed true
        } else {
            Mouse.setGrabbed false
        }
    }

    public WorldShader getWorldShader() { defaultShader }

    //TODO move frustum calc, light pos, etc into GameRenderer

}
