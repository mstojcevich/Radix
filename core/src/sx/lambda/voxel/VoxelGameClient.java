package sx.lambda.voxel;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.spacehq.mc.protocol.data.game.ItemStack;
import org.spacehq.mc.protocol.data.game.Position;
import org.spacehq.mc.protocol.data.game.values.Face;
import org.spacehq.mc.protocol.data.game.values.entity.player.PlayerAction;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPlaceBlockPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientSwingArmPacket;
import pw.oxcafebabe.marcusant.eventbus.EventListener;
import pw.oxcafebabe.marcusant.eventbus.Priority;
import pw.oxcafebabe.marcusant.eventbus.exceptions.InvalidListenerException;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.api.events.EventEarlyInit;
import sx.lambda.voxel.api.events.EventWorldStart;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.client.gui.GuiScreen;
import sx.lambda.voxel.client.gui.screens.ChatGUI;
import sx.lambda.voxel.client.gui.screens.IngameHUD;
import sx.lambda.voxel.client.gui.screens.MainMenu;
import sx.lambda.voxel.client.gui.transition.SlideUpAnimation;
import sx.lambda.voxel.client.gui.transition.TransitionAnimation;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.entity.EntityRotation;
import sx.lambda.voxel.entity.player.Player;
import sx.lambda.voxel.net.mc.client.MinecraftClientConnection;
import sx.lambda.voxel.render.NotInitializedException;
import sx.lambda.voxel.render.Renderer;
import sx.lambda.voxel.render.game.GameRenderer;
import sx.lambda.voxel.settings.SettingsManager;
import sx.lambda.voxel.tasks.*;
import sx.lambda.voxel.texture.TextureManager;
import sx.lambda.voxel.util.PlotCell3f;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.util.gl.ShaderManager;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.World;
import sx.lambda.voxel.world.chunk.IChunk;

import javax.swing.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.badlogic.gdx.graphics.GL20.*;

public class VoxelGameClient extends ApplicationAdapter {

    public static final boolean DEBUG = false;
    public static final String GAME_TITLE = "VoxelTest";
    private static VoxelGameClient theGame;
    public int chunkRenderTimes = 0;
    public int numChunkRenders = 0;
    private SettingsManager settingsManager;
    private IWorld world;
    private Player player;
    private boolean done;
    private Vec3i selectedBlock;
    private Vec3i selectedNextPlace;
    private Queue<Runnable> glQueue = new ConcurrentLinkedDeque<>();
    private MainMenu mainMenu;
    private IngameHUD hud;
    private ChatGUI chatGUI;
    private GuiScreen currentScreen;
    private TextureManager textureManager = new TextureManager();
    private ShaderManager shaderManager = new ShaderManager();
    private Renderer renderer;
    private TransitionAnimation transitionAnimation;
    private boolean remote;
    private GameRenderer gameRenderer;
    private Texture blockTextureAtlas;
    private PerspectiveCamera camera;
    private OrthographicCamera hudCamera;
    private SpriteBatch guiBatch;

    private MinecraftClientConnection mcClientConn;

    private MovementHandler movementHandler = new MovementHandler(this);
    private RepeatedTask[] handlers = new RepeatedTask[]{new WorldLoader(this), movementHandler, new EntityUpdater(this), new LightUpdater(this)};

    // Android specific stuff
    private boolean android;
    private Stage androidStage;
    private Skin androidOverlaySkin;
    private TouchpadStyle touchpadStyle;
    private TextureAtlas touchControlsAtlas;
    private Touchpad moveTouchpad, rotateTouchpad;
    private ImageButton jumpButton, placeButton, breakButton;

    public static VoxelGameClient getInstance() {
        return theGame;
    }

    @Override
    public void create() {
        theGame = this;

        this.android = Gdx.app.getType().equals(Application.ApplicationType.Android);

        try {
            VoxelGameAPI.instance.registerBuiltinBlocks();
        } catch (VoxelGameAPI.BlockRegistrationException e) {
            e.printStackTrace();
        }
        VoxelGameAPI.instance.registerMinecraftBiomes();
        try {
            VoxelGameAPI.instance.getEventManager().register(this);
        } catch (InvalidListenerException e) {
            e.printStackTrace();
        }
        VoxelGameAPI.instance.getEventManager().push(new EventEarlyInit());

        settingsManager = new SettingsManager();
        this.setupOGL();

        gameRenderer = new GameRenderer(this);
        hud = new IngameHUD();
        chatGUI = new ChatGUI();
        chatGUI.init();
        mainMenu = new MainMenu();
        setCurrentScreen(mainMenu);

        this.startHandlers();
    }

    private void setupTouchControls() {
        // Create touch control element atlas
        touchControlsAtlas = new TextureAtlas(Gdx.files.internal("textures/gui/touch/touch.atlas"));
        // Create skin
        androidOverlaySkin = new Skin();
        for(TextureAtlas.AtlasRegion r : touchControlsAtlas.getRegions()) {
            androidOverlaySkin.add(r.name, r, TextureRegion.class);
        }

        // Create touchpad skin/style
        touchpadStyle = new TouchpadStyle();
        touchpadStyle.background = androidOverlaySkin.getDrawable("touchpadBackground");
        touchpadStyle.knob = androidOverlaySkin.getDrawable("touchpadKnob");

        moveTouchpad = new Touchpad(10, touchpadStyle);
        moveTouchpad.setBounds(15, 15, 200, 200);

        rotateTouchpad = new Touchpad(10, touchpadStyle);
        rotateTouchpad.setBounds(1280 - 200 - 15, 15, 200, 200);

        jumpButton = new ImageButton(androidOverlaySkin.getDrawable("jumpButtonIcon"));
        jumpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                movementHandler.jump();
            }
        });
        placeButton = new ImageButton(androidOverlaySkin.getDrawable("placeButtonIcon"));
        placeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                placeBlock();
            }
        });
        breakButton = new ImageButton(androidOverlaySkin.getDrawable("breakButtonIcon"));
        breakButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                breakBlock();
            }
        });
        ImageButton[] buttons = new ImageButton[]{
                jumpButton, placeButton, breakButton
        };
        int totalButtonWidth = 0;
        for(ImageButton button : buttons) {
            totalButtonWidth += button.getImage().getPrefWidth();
        }
        int btnX = 1280/2-totalButtonWidth/2;
        for(ImageButton button : buttons) {
            button.setBounds(btnX, 0, button.getImage().getPrefWidth(), button.getImage().getPrefHeight());
            btnX += button.getImage().getPrefWidth();
        }

        // Setup android stage
        androidStage = new Stage(new FitViewport(1280, 720));
        androidStage.addActor(moveTouchpad);
        androidStage.addActor(rotateTouchpad);
        for(ImageButton button : buttons) {
            androidStage.addActor(button);
        }
    }

    private void startHandlers() {
        for (RepeatedTask r : handlers) {
            new Thread(r, r.getIdentifier()).start();
        }
    }

    private void setupOGL() {
        Gdx.gl.glEnable(GL_TEXTURE_2D);
        Gdx.gl.glEnable(GL_DEPTH_TEST);//Enable depth visibility check
        Gdx.gl.glDepthFunc(GL_LEQUAL);//How to test depth (less than or equal)

        camera = new PerspectiveCamera(90, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 150f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();
        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        guiBatch = new SpriteBatch();
        guiBatch.setProjectionMatrix(hudCamera.combined);

        if(android) {
            setupTouchControls();
        }

        Gdx.input.setInputProcessor(new VoxelGameGdxInputHandler(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        done = true;
        if (isRemote()) {
            if(mcClientConn != null)
                mcClientConn.getClient().getSession().disconnect("Closing game");
        }

        if(android) {
            androidStage.dispose();
            androidOverlaySkin.dispose();
            touchControlsAtlas.dispose();
        }
    }

    @Override
    public void render() {
        try {
            if (done) Gdx.app.exit();

            prepareNewFrame();

            runQueuedOGL();

            if (renderer != null) {
                renderer.render();
            }

            guiBatch.begin();
            if (renderer != null) {
                renderer.draw2d(guiBatch);
            }

            if (currentScreen != null) {
                currentScreen.render(world != null, guiBatch);
            }

            if(transitionAnimation != null) {
                transitionAnimation.render(guiBatch);
                if(transitionAnimation.isFinished()) {
                    transitionAnimation.finish();
                    transitionAnimation = null;
                }
            }

            guiBatch.end();

            if(world != null && android) {
                updatePositionRotationAndroid();
                androidStage.act();
                androidStage.draw();
            }

        } catch (Exception e) {
            done = true;
            e.printStackTrace();
            Gdx.input.setCursorCatched(false);
            Gdx.app.exit();
        }

    }

    private void runQueuedOGL() {
        Runnable currentRunnable;
        while ((currentRunnable = glQueue.poll()) != null) {
            currentRunnable.run();
        }

    }

    private void prepareNewFrame() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.4f, 0.6f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void updateSelectedBlock() {
        PlotCell3f plotter = new PlotCell3f(0, 0, 0, 1, 1, 1);
        float x = player.getPosition().getX();
        float y = player.getPosition().getY() + player.getEyeHeight();
        float z = player.getPosition().getZ();
        float pitch = player.getRotation().getPitch();
        float yaw = player.getRotation().getYaw();
        float reach = player.getReach();

        float deltaX = (float) (Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)));
        float deltaY = (float) (Math.sin(Math.toRadians(pitch)));
        float deltaZ = (float) (-Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)));

        plotter.plot(new Vector3(x, y, z), new Vector3(deltaX, deltaY, deltaZ), MathUtils.ceil(reach * reach));
        Vec3i last = null;
        while (plotter.next()) {
            Vec3i v = plotter.get();
            Vec3i bp = new Vec3i(v.x, v.y, v.z);
            IChunk theChunk = world.getChunkAtPosition(bp);
            if (theChunk != null) {
                Block b = theChunk.getBlockAtPosition(bp);
                if (b != null && b.isSelectable()) {
                    selectedBlock = bp;
                    if (last != null) {
                        if (theChunk.getBlockAtPosition(last) == null) {
                            selectedNextPlace = last;
                        }
                    }

                    plotter.end();
                    return;
                }

                last = bp;
            }

        }

        selectedNextPlace = null;
        selectedBlock = null;
    }

    public void addToGLQueue(Runnable runnable) {
        glQueue.add(runnable);
    }

    public IWorld getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public ShaderManager getShaderManager() {
        return shaderManager;
    }

    public boolean isDone() {
        return done;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Vec3i getSelectedBlock() {
        return selectedBlock;
    }

    public Vec3i getNextPlacePos() {
        return selectedNextPlace;
    }

    public void startShutdown() {
        done = true;
    }

    private void setRenderer(Renderer renderer) {
        if (renderer == null) {
            if (this.renderer != null) {
                synchronized (this.renderer) {
                    this.renderer.cleanup();
                    this.renderer = null;
                }

            } else {
                this.renderer = null;
            }

        } else if (this.renderer == null) {
            this.renderer = renderer;
            this.renderer.init();
        } else if (!this.renderer.equals(renderer)) {
            synchronized (this.renderer) {
                this.renderer.cleanup();
                this.renderer = renderer;
                this.renderer.init();
            }

        }

    }

    public boolean isRemote() {
        return remote;
    }

    public MinecraftClientConnection getMinecraftConn() {
        return mcClientConn;
    }

    public void handleCriticalException(Exception ex) {
        ex.printStackTrace();
        this.done = true;
        Gdx.input.setCursorCatched(false);
        if(!android) {
            JOptionPane.showMessageDialog(null, GAME_TITLE + " crashed. " + String.valueOf(ex), GAME_TITLE + " crashed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public GuiScreen getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(GuiScreen screen) {
        if (currentScreen == null) {
            currentScreen = screen;
            screen.init();
        } else if (!currentScreen.equals(screen)) {
            synchronized (currentScreen) {
                currentScreen.finish();
                currentScreen = screen;
                screen.init();
            }

        }

        if (screen.equals(hud) && !android) {
            Gdx.input.setCursorCatched(true);
        } else {
            Gdx.input.setCursorCatched(false);
        }

    }

    public IngameHUD getHud() {
        return this.hud;
    }

    public ChatGUI getChatGUI() {
        return this.chatGUI;
    }

    public GameRenderer getGameRenderer() {
        return this.gameRenderer;
    }

    public void enterRemoteWorld(final String hostname, final short port) {
        enterWorld(new World(true, false), true);

        new Thread("Client Connection") {
            @Override
            public void run() {
                try {
                    (mcClientConn = new MinecraftClientConnection(VoxelGameClient.this, hostname, port)).start();
                    chatGUI.setup(mcClientConn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    public void enterLocalWorld(IWorld world) {
        enterWorld(world, false);
    }

    public void exitWorld() {
        addToGLQueue(new Runnable() {
            @Override
            public void run() {
                setCurrentScreen(mainMenu);
                setRenderer(null);
                getWorld().cleanup();
                if (isRemote()) {
                    mcClientConn.getClient().getSession().disconnect("Exiting world");
                }

                world = null;
                remote = false;
                player = null;
            }

        });

        if(android) {
            Gdx.input.setInputProcessor(new VoxelGameGdxInputHandler(this));
        }
    }

    private void enterWorld(final IWorld world, final boolean remote) {
        this.world = world;
        this.remote = remote;
        player = new Player(new EntityPosition(0, 256, 0), new EntityRotation(0, 0));
        glQueue.add(new Runnable() {
            @Override
            public void run() {
                setRenderer(getGameRenderer());
                getPlayer().init();
                world.addEntity(getPlayer());
                transitionAnimation = new SlideUpAnimation(getCurrentScreen(), 1000);
                transitionAnimation.init();
                setCurrentScreen(getHud());

                if (!remote) {
                    world.loadChunks(new EntityPosition(0, 0, 0), getSettingsManager().getVisualSettings().getViewDistance());
                }


                VoxelGameAPI.instance.getEventManager().push(new EventWorldStart());
                // Delays are somewhere in this function. Above here.
            }

        });

        if(android) {
            Gdx.input.setInputProcessor(androidStage);
        }
    }

    public Texture getBlockTextureAtlas() throws NotInitializedException {
        if (blockTextureAtlas == null) throw new NotInitializedException();
        return blockTextureAtlas;
    }

    @EventListener(Priority.LAST)
    public void onBlockRegister(EventEarlyInit event) {
        // Create a texture atlas for all of the blocks
        addToGLQueue(new Runnable() {
            @Override
            public void run() {
                Pixmap bi = new Pixmap(1024, 1024, Pixmap.Format.RGBA8888);
                bi.setColor(1, 1, 1, 1);
                final int BLOCK_TEX_SIZE = 32;
                int textureIndex = 0;
                for (Block b : VoxelGameAPI.instance.getBlocks()) {
                    b.setTextureIndex(textureIndex);
                    for(String texLoc : b.getTextureLocations()) {
                        int x = textureIndex * BLOCK_TEX_SIZE % bi.getWidth();
                        int y = BLOCK_TEX_SIZE * ((textureIndex * BLOCK_TEX_SIZE) / bi.getWidth());
                        Pixmap tex = new Pixmap(Gdx.files.internal(texLoc));
                        bi.drawPixmap(tex, x, y);
                        tex.dispose();
                        textureIndex++;
                    }
                }

                blockTextureAtlas = new Texture(bi);
                bi.dispose();
            }

        });
    }

    @Override
    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
        guiBatch.setProjectionMatrix(hudCamera.combined);

        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        if(android)
            androidStage.getViewport().update(width, height, true);
    }

    public boolean onAndroid() {
        return android;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public OrthographicCamera getHudCamera() {
        return hudCamera;
    }

    public MovementHandler getMovementHandler() {
        return movementHandler;
    }

    public void breakBlock() {
        if (this.getSelectedBlock() != null
                && world.getChunkAtPosition(selectedBlock).getBlockAtPosition(selectedBlock).isSelectable()
                && this.currentScreen == this.hud) {
            if (this.isRemote()) {
                mcClientConn.getClient().getSession().send(new ClientSwingArmPacket());
                mcClientConn.getClient().getSession().send(new ClientPlayerActionPacket(PlayerAction.START_DIGGING, new Position(selectedBlock.x, selectedBlock.y, selectedBlock.z), Face.TOP));
                mcClientConn.getClient().getSession().send(new ClientPlayerActionPacket(PlayerAction.FINISH_DIGGING, new Position(selectedBlock.x, selectedBlock.y, selectedBlock.z), Face.TOP));
            } else {
                this.getWorld().removeBlock(this.getSelectedBlock().x, this.getSelectedBlock().y, this.getSelectedBlock().z);
            }
            updateSelectedBlock();
        }
    }

    public void placeBlock() {
        if (this.getNextPlacePos() != null && this.currentScreen == this.hud) {
            if (this.isRemote() && this.mcClientConn != null) {
                this.mcClientConn.getClient().getSession().send(new ClientPlayerPlaceBlockPacket(
                        new Position(this.getNextPlacePos().x, this.getNextPlacePos().y, this.getNextPlacePos().z),
                        Face.TOP /* TODO MCPROTO send correct face */, new ItemStack(player.getItemInHand()), 0, 0, 0));
            } else {
                this.getWorld().addBlock(this.getPlayer().getItemInHand(), this.getNextPlacePos().x, this.getNextPlacePos().y, this.getNextPlacePos().z);
            }
            updateSelectedBlock();
        }
    }

    private void updatePositionRotationAndroid() {
        if(!MathUtils.isZero(rotateTouchpad.getKnobPercentX()) || !MathUtils.isZero(rotateTouchpad.getKnobPercentY())) {
            float rotMult = 200 * Gdx.graphics.getDeltaTime();

            player.getRotation().offset(rotateTouchpad.getKnobPercentY() * rotMult, rotateTouchpad.getKnobPercentX() * rotMult);
            updateSelectedBlock();
            gameRenderer.calculateFrustum();
        }

        if(!MathUtils.isZero(moveTouchpad.getKnobPercentX()) || !MathUtils.isZero(moveTouchpad.getKnobPercentY())) {
            float posMult = 4.5f * Gdx.graphics.getDeltaTime();

            float newX = player.getPosition().getX();
            float newZ = player.getPosition().getZ();
            float yawSine = MathUtils.sinDeg(player.getRotation().getYaw());
            float yawCosine = MathUtils.cosDeg(player.getRotation().getYaw());
            newX += yawSine * moveTouchpad.getKnobPercentY() * posMult;
            newZ += -yawCosine * moveTouchpad.getKnobPercentY() * posMult;
            newX += yawCosine * moveTouchpad.getKnobPercentX() * posMult;
            newZ += yawSine * moveTouchpad.getKnobPercentX() * posMult;

            int blockX = MathUtils.floor(newX);
            int blockZ = MathUtils.floor(newZ);
            int blockFeetY = MathUtils.floor(player.getPosition().getY() - 0.1f);
            int blockHeadY = MathUtils.floor(player.getPosition().getY() + player.getHeight() - 0.1f);
            if(!movementHandler.checkCollision(blockX, blockFeetY, blockZ)
                    && !movementHandler.checkCollision(blockX, blockHeadY, blockZ)) {
                player.getPosition().setPos(newX, player.getPosition().getY(), newZ);
                gameRenderer.calculateFrustum();
            }
        }
    }

}
