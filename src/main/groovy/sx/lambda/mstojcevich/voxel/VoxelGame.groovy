package sx.lambda.mstojcevich.voxel

import groovy.transform.CompileStatic
import io.netty.channel.ChannelHandlerContext
import org.lwjgl.BufferUtils
import org.lwjgl.LWJGLException
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.util.glu.GLU
import org.newdawn.slick.Color
import org.newdawn.slick.UnicodeFont
import org.newdawn.slick.font.effects.ColorEffect
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI
import sx.lambda.mstojcevich.voxel.api.events.EventGameTick
import sx.lambda.mstojcevich.voxel.api.events.EventWorldStart
import sx.lambda.mstojcevich.voxel.client.gui.GuiScreen
import sx.lambda.mstojcevich.voxel.client.gui.screens.IngameHUD
import sx.lambda.mstojcevich.voxel.client.gui.screens.MainMenu
import sx.lambda.mstojcevich.voxel.entity.EntityRotation
import sx.lambda.mstojcevich.voxel.net.packet.client.PacketLeaving
import sx.lambda.mstojcevich.voxel.render.Renderer
import sx.lambda.mstojcevich.voxel.settings.SettingsManager
import sx.lambda.mstojcevich.voxel.shader.PostProcessShader
import sx.lambda.mstojcevich.voxel.tasks.InputHandler
import sx.lambda.mstojcevich.voxel.tasks.MovementHandler
import sx.lambda.mstojcevich.voxel.tasks.RepeatedTask
import sx.lambda.mstojcevich.voxel.util.Frustum
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
import java.awt.Font
import java.nio.FloatBuffer
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentLinkedDeque

import static org.lwjgl.opengl.GL11.*

@CompileStatic
public class VoxelGame {

    public static final boolean DEBUG = false

    private static VoxelGame theGame

    private SettingsManager settingsManager

    public static final String GAME_TITLE = "VoxelTest"

    private int renderedFrames = 0;

    private IWorld world

    private Player player

    private boolean done

    private synchronized Vec3i selectedBlock, selectedNextPlace

    private int hudDisplayList

    private Queue<Runnable> glQueue = new ConcurrentLinkedDeque<>()

    private MainMenu mainMenu = new MainMenu()
    private GuiScreen currentScreen

    private int fps
    public int chunkRenderTimes = 0 // TODO move to World
    public int numChunkRenders = 0 // TODO move to World

    private TextureManager textureManager = new TextureManager()
    private ShaderManager shaderManager = new ShaderManager()

    private Frustum frustum = new Frustum()

    private boolean calcFrustum

    private FloatBuffer lightPosition

    private Renderer renderer

    private ShaderProgram defaultShader
    private PostProcessShader postProcessShader

    private final boolean remote;
    private String hostname
    private short port
    private ChannelHandlerContext serverChanCtx;

    private RepeatedTask[] handlers = [
        new WorldLoader(this),
        new InputHandler(this),
        new MovementHandler(this),
        new RotationHandler(this)
    ]

    public static void main(String[] args) throws Exception {
        theGame = new VoxelGame("127.0.0.1", (short)31173);
        theGame.start();
    }

    VoxelGame() {
        remote = false
    }

    VoxelGame(String hostname, short port) {
        remote = true
        this.hostname = hostname
        this.port = port
    }

    private void start() throws LWJGLException {
        settingsManager = new SettingsManager()
        setupWindow();
        this.setupOGL();

        world = new World(remote, false)
        player = new Player(new EntityPosition(0, 256, 0), new EntityRotation(0, 0))
        player.init()
        world.addEntity(player)
        currentScreen = new IngameHUD()
        currentScreen.init()
        this.startHandlers()
        this.setRenderer(new GameRenderer(this))
        this.hudDisplayList = glGenLists(1)
        rerenderHud()
        if(remote) {
            new Thread("Client Connection") {
                @Override
                public void run() {
                    new ClientConnection(hostname, port).start()
                }
            }.start()
        }
        if(!remote) {
            world.loadChunks(new EntityPosition(0, 0, 0), getSettingsManager().getVisualSettings().getViewDistance())
        }

        new Thread("Entity Update") {
            @Override
            public void run() {
                try {
                    while (!done) {
                        player.onUpdate()

                        VoxelGameAPI.instance.eventManager.push(new EventGameTick(world))

                        sleep(50l);
                    }
                } catch(Exception ex) {
                    handleCriticalException(ex)
                }
            }
        }.start()

        VoxelGameAPI.instance.eventManager.push(new EventWorldStart())

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
        Display.setResizable true

        Display.create()

        Mouse.setGrabbed true
    }

    private void setupOGL() {
        glEnable GL_TEXTURE_2D
        glShadeModel GL_SMOOTH
        glClearColor(0.2f, 0.4f, 1, 0) //Set default color
        glClearDepth 1 //Set default depth buffer depth
        glEnable GL_DEPTH_TEST //Enable depth visibility check
        glDepthFunc GL_LEQUAL //How to test depth (less than or equal)

        glEnableClientState GL_VERTEX_ARRAY
        glEnableClientState GL_COLOR_ARRAY

        glMatrixMode GL_PROJECTION //Currently altering projection matrix
        glLoadIdentity()

        frustum.calculateFrustum()

        glMatrixMode GL_MODELVIEW //Currently altering modelview matrix
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)
        glEnable GL_CULL_FACE
        glFrontFace GL_CCW

        lightPosition = BufferUtils.createFloatBuffer(4)
        lightPosition.put(0).put(256).put(0).put(1).flip()

        glEnable GL_LIGHTING
        glEnable GL_LIGHT0

        FloatBuffer diffuseVec = BufferUtils.createFloatBuffer(4)
        diffuseVec.put(2, 2, 2, 1.0f)
        diffuseVec.flip()

        glLightModel(GL_LIGHT_MODEL_AMBIENT, diffuseVec)

        defaultShader = createShader("default", ShaderProgram.class)
        postProcessShader = createShader("post-process", PostProcessShader.class)

        getShaderManager().setShader(defaultShader)
    }

    private void run() {
        long startTime = System.currentTimeMillis()
        try {
            while (!Display.isCloseRequested() && !done) {
                if(Display.wasResized()) {
                    shaderManager.updateScreenSize()
                }

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
            glPushMatrix()

            renderer.render()

            glPopMatrix()
        }

        //2D starts here
        glPushMatrix()
        glPushAttrib GL_ENABLE_BIT
        prepare2D()

        glEnable(GL_BLEND)

        if(renderer != null) {
            renderer.draw2d()
        }

//        shaderManager.disableTexturing()
//        glDisableClientState(GL_NORMAL_ARRAY)
//        glDisableClientState(GL_TEXTURE_COORD_ARRAY)
//        mainMenu.render(true)
//        glEnableClientState(GL_NORMAL_ARRAY)
//        glEnableClientState(GL_TEXTURE_COORD_ARRAY)
//        shaderManager.enableTexturing()

        glCallList hudDisplayList //TODO move to HUD GUI
        currentScreen.render(true) //Render as ingame

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
        shaderManager.disableLighting()
    }

    private void prepareNewFrame() {
        if(!settingsManager.visualSettings.postProcessEnabled) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        }
        glMatrixMode GL_PROJECTION //Currently altering projection matrix
        glLoadIdentity()

        float camNear = 0.1f
        float camFar = settingsManager.visualSettings.viewDistance * world.getChunkSize()
        GLU.gluPerspective(100, (float) Display.getWidth() / Display.getHeight(), camNear, camFar)
        shaderManager.onPerspective(camNear, camFar)
        //Set up camera

        glMatrixMode GL_MODELVIEW //Currently altering modelview matrix
        glLoadIdentity()

        getTextureManager().bindTexture(-1)

        shaderManager.enableLighting()

        shaderManager.setAnimTime((int)(System.currentTimeMillis() % 100000));
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

    public void rerenderHud() {
        if(player == null || world == null)return
        glNewList(hudDisplayList, GL_COMPILE)
        player.getItemInHand().getRenderer().render2d(0, 0, 20)
        glEndList()
    }

    public IWorld getWorld() { world }

    public Player getPlayer() { player }

    public TextureManager getTextureManager() { textureManager }

    public ShaderManager getShaderManager() { shaderManager }

    public Frustum getFrustum() { frustum }

    public boolean isDone() { done }

    public SettingsManager getSettingsManager() { settingsManager }

    public Vec3i getSelectedBlock() { selectedBlock }

    public Vec3i getNextPlacePos() { selectedNextPlace }

    public void startShutdown() {
        done = true
    }

    public void calculateFrustum() {
        this.calcFrustum = true
    }

    public boolean shouldCalcFrustum() { calcFrustum }

    public void dontCalcFrustum() { calcFrustum = false } //TODO remove this when frustum stuff is in GameRenderer

    public FloatBuffer getLightPosition() { lightPosition }

    private void setRenderer(Renderer renderer) {
        this.renderer = renderer
        this.renderer.init()
    }

    public <T extends ShaderProgram> T createShader(String shaderName, Class<T> type) {
        String nameNoExt = "/shaders/$shaderName/$shaderName"
        String vertex = this.getClass().getResourceAsStream(nameNoExt + ".vert").getText()
        String fragment = this.getClass().getResourceAsStream(nameNoExt + ".frag").getText()

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

    public PostProcessShader getPostProcessShader() { postProcessShader }

    public int getFps() { fps }

    //TODO move frustum calc, light pos, etc into GameRenderer

}
