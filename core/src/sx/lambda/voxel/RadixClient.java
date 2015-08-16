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
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.api.events.EventEarlyInit;
import sx.lambda.voxel.api.events.EventWorldStart;
import sx.lambda.voxel.api.events.register.EventRegisterBiomes;
import sx.lambda.voxel.api.events.register.EventRegisterBlockRenderers;
import sx.lambda.voxel.api.events.register.EventRegisterItems;
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
import sx.lambda.voxel.tasks.EntityUpdater;
import sx.lambda.voxel.tasks.MovementHandler;
import sx.lambda.voxel.tasks.RepeatedTask;
import sx.lambda.voxel.tasks.WorldLoader;
import sx.lambda.voxel.util.PlotCell3f;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.World;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

import javax.swing.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT;

/**
 * Primary class for an instance of a Radix client.
 */
public class RadixClient extends ApplicationAdapter {

    public static final String GAME_TITLE = "VoxelTest";
    private static RadixClient theGame;

    private SettingsManager settingsManager;

    private boolean done; // Whether the game has finished and should shut down

    private boolean remote; // Whether the player is connected to a remote server
    private IWorld world;
    private Player player;
    private Vec3i selectedBlock; // selected block to break next
    private Vec3i selectedNextPlace; // selected block to place at next

    private MinecraftClientConnection mcClientConn;

    private final Queue<Runnable> glQueue = new ConcurrentLinkedDeque<>();

    private GuiScreen currentScreen;
    private TransitionAnimation transitionAnimation;
    private Renderer renderer;
    private GameRenderer gameRenderer; // renderer for when in game
    private MainMenu mainMenu;
    private IngameHUD hud;
    private ChatGUI chatGUI;

    private Texture blockTextureAtlas;
    private PerspectiveCamera camera;
    private OrthographicCamera hudCamera;
    private SpriteBatch guiBatch;

    private final MovementHandler movementHandler = new MovementHandler(this);
    private final RepeatedTask[] handlers = new RepeatedTask[]{new WorldLoader(this), movementHandler, new EntityUpdater(this)};

    // Android specific stuff
    private boolean android;
    private Stage androidStage;
    private Skin androidOverlaySkin;
    private TouchpadStyle touchpadStyle;
    private TextureAtlas touchControlsAtlas;
    private Touchpad moveTouchpad, rotateTouchpad;
    private ImageButton jumpButton, placeButton, breakButton;

    private boolean wireframe, debugText;

    public static RadixClient getInstance() {
        return theGame;
    }

    @Override
    public void create() {
        theGame = this;
        settingsManager = new SettingsManager();

        this.android = Gdx.app.getType().equals(Application.ApplicationType.Android);

        try {
            RadixAPI.instance.getEventManager().register(this);
        } catch (InvalidListenerException e) {
            e.printStackTrace();
        }

        try {
            RadixAPI.instance.registerBuiltinBlockRenderers();
            RadixAPI.instance.getEventManager().push(new EventRegisterBlockRenderers());
        } catch (RadixAPI.DuplicateRendererException ex) {
            ex.printStackTrace();
        }

        try {
            RadixAPI.instance.registerBuiltinItems();
            if(!settingsManager.getVisualSettings().isFancyTreesEnabled()) {
                RadixAPI.instance.getBlock(BuiltInBlockIds.LEAVES_ID).setOccludeCovered(true);
                RadixAPI.instance.getBlock(BuiltInBlockIds.LEAVES_TWO_ID).setOccludeCovered(true);
            }

            RadixAPI.instance.getEventManager().push(new EventRegisterItems());
        } catch (RadixAPI.BlockRegistrationException e) {
            e.printStackTrace();
        }

        RadixAPI.instance.registerMinecraftBiomes();
        RadixAPI.instance.getEventManager().push(new EventRegisterBiomes());

        RadixAPI.instance.getEventManager().push(new EventEarlyInit());

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
        camera = new PerspectiveCamera(90, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 150f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 450f;
        camera.update();
        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        guiBatch = new SpriteBatch();
        guiBatch.setProjectionMatrix(hudCamera.combined);

        if(android) {
            setupTouchControls();
        }

        Gdx.input.setInputProcessor(new RadixInputHandler(this));

        if(settingsManager.getVisualSettings().nonContinuous()) {
            Gdx.graphics.setContinuousRendering(false);
        }
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

            if(settingsManager.getVisualSettings().finishEachFrame())
                Gdx.gl.glFinish();
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
        int clear = GL_DEPTH_BUFFER_BIT;
        if(world == null || getCurrentScreen() != hud || (transitionAnimation != null && !transitionAnimation.isFinished())) {
            /*
            Clear color buffer too when not in a world
            When in a world, since there is a skybox, clearing the color buffer would be pointless.
             */
            clear |= GL_COLOR_BUFFER_BIT;
            Gdx.gl.glClearColor(0.7f, 0.8f, 1f, 1f);
        }
        Gdx.gl.glClear(clear);
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

        final Vec3i oldPos = selectedBlock;

        plotter.plot(new Vector3(x, y, z), new Vector3(deltaX, deltaY, deltaZ), MathUtils.ceil(reach * reach));
        Vec3i last = null;
        while (plotter.next()) {
            Vec3i v = plotter.get();
            Vec3i bp = new Vec3i(v.x, v.y, v.z);
            IChunk theChunk = world.getChunk(bp);
            if (theChunk != null) {
                if(bp.y >= world.getHeight())
                    continue;
                try {
                    Block b = theChunk.getBlock(bp.x & (world.getChunkSize() - 1), bp.y, bp.z & (world.getChunkSize() - 1));
                    if (b != null && b.isSelectable()) {
                        selectedBlock = bp;
                        if (last != null) {
                            Block lastBlock = theChunk.getBlock(last.x & (world.getChunkSize() - 1), last.y, last.z & (world.getChunkSize() - 1));
                            if (lastBlock == null || !lastBlock.isSelectable()) {
                                selectedNextPlace = last;
                            }
                        }

                        plotter.end();
                        if(!selectedBlock.equals(oldPos)) {
                            player.resetBlockBreak();
                        }
                        return;
                    }
                } catch(CoordinatesOutOfBoundsException ex) {
                    ex.printStackTrace();
                    plotter.end();
                    player.resetBlockBreak();
                    return;
                }

                last = bp;
            }

        }

        selectedNextPlace = null;
        selectedBlock = null;
        if(player != null) {
            player.resetBlockBreak();
        }
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
                this.renderer.cleanup();
                this.renderer = null;
            }
        } else if (this.renderer == null) {
            this.renderer = renderer;
            this.renderer.init();
        } else {
            this.renderer.cleanup();
            this.renderer = renderer;
            this.renderer.init();
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
            currentScreen.finish();
            currentScreen = screen;
            screen.init();
        }

        if (screen.equals(hud) && !android) {
            Gdx.input.setCursorCatched(true);
        } else {
            Gdx.input.setCursorCatched(false);
        }
    }

    public void enterRemoteWorld(final String hostname, final short port) {
        enterWorld(new World(true, false), true);
        (mcClientConn = new MinecraftClientConnection(RadixClient.this, hostname, port)).start();
        chatGUI.setup(mcClientConn);
    }

    public void enterLocalWorld(IWorld world) {
        enterWorld(world, false);
    }

    public void exitWorld() {
        addToGLQueue(() -> {
            setCurrentScreen(mainMenu);
            setRenderer(null);
            getWorld().cleanup();
            if (isRemote()) {
                mcClientConn.getClient().getSession().disconnect("Exiting world");
            }

            world = null;
            remote = false;
            player = null;
        });

        if(android) {
            Gdx.input.setInputProcessor(new RadixInputHandler(this));
        }
    }

    private void enterWorld(final IWorld world, final boolean remote) {
        this.world = world;
        this.remote = remote;
        player = new Player(new EntityPosition(0, 256, 0), new EntityRotation(0, 0));
        addToGLQueue(() -> {
            setRenderer(getGameRenderer());
            getPlayer().init();
            world.addEntity(getPlayer());
            transitionAnimation = new SlideUpAnimation(getCurrentScreen(), 1000);
            transitionAnimation.init();
            setCurrentScreen(getHud());

            if (!remote) {
                world.loadChunks(new EntityPosition(0, 0, 0), getSettingsManager().getVisualSettings().getViewDistance());
            }

            RadixAPI.instance.getEventManager().push(new EventWorldStart());
            // Delays are somewhere in this function. Above here.
        });

        if(android) {
            Gdx.input.setInputProcessor(androidStage);
        }
    }

    public Texture getBlockTextureAtlas() throws NotInitializedException {
        if (blockTextureAtlas == null) throw new NotInitializedException();
        return blockTextureAtlas;
    }

    @EventListener(Priority.FIRST)
    public void createBlockTextureMap(EventEarlyInit event) {
        // Create a texture atlas for all of the blocks
        addToGLQueue(() -> {
            Pixmap bi = new Pixmap(2048, 2048, Pixmap.Format.RGBA8888);
            bi.setColor(1, 1, 1, 1);
            final int BLOCK_TEX_SIZE = 32;
            int textureIndex = 0;
            for (Block b : RadixAPI.instance.getBlocks()) {
                if(b == null)
                    continue;
                b.setTextureIndex(textureIndex);
                for (String texLoc : b.getTextureLocations()) {
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
        });
    }

    @Override
    public void resize(int width, int height) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
        guiBatch.setProjectionMatrix(hudCamera.combined);

        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        if(android)
            androidStage.getViewport().update(width, height, true);
    }

    public void beginBreak() {
        if (this.getSelectedBlock() != null
                && this.currentScreen == this.hud) {
            IChunk chunk = world.getChunk(selectedBlock);
            if(chunk != null) {
                try {
                    Block block = chunk.getBlock(selectedBlock.x & (world.getChunkSize() - 1), selectedBlock.y, selectedBlock.z & (world.getChunkSize() - 1));
                    if (block != null && block.isSelectable()) {
                        if (this.isRemote()) {
                            mcClientConn.getClient().getSession().send(new ClientSwingArmPacket());
                            mcClientConn.getClient().getSession().send(new ClientPlayerActionPacket(PlayerAction.START_DIGGING, new Position(selectedBlock.x, selectedBlock.y, selectedBlock.z), Face.TOP));
                        }
                    }
                } catch (CoordinatesOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void cancelBreak() {
        if (this.getSelectedBlock() != null
                && this.currentScreen == this.hud) {
            IChunk chunk = world.getChunk(selectedBlock);
            if(chunk != null) {
                try {
                    Block block = chunk.getBlock(selectedBlock.x & (world.getChunkSize() - 1), selectedBlock.y, selectedBlock.z & (world.getChunkSize() - 1));
                    if (block != null && block.isSelectable()) {
                        if (this.isRemote()) {
                            mcClientConn.getClient().getSession().send(new ClientPlayerActionPacket(PlayerAction.CANCEL_DIGGING, new Position(selectedBlock.x, selectedBlock.y, selectedBlock.z), Face.TOP));
                        }
                    }
                    player.resetBlockBreak();
                } catch (CoordinatesOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void breakBlock() {
        if (this.getSelectedBlock() != null
                && this.currentScreen == this.hud) {
            IChunk chunk = world.getChunk(selectedBlock);
            if(chunk != null) {
                try {
                    Block block = chunk.getBlock(selectedBlock.x & (world.getChunkSize() - 1), selectedBlock.y, selectedBlock.z & (world.getChunkSize() - 1));
                    if (block != null && block.isSelectable()) {
                        if (this.isRemote()) {
                            mcClientConn.getClient().getSession().send(new ClientSwingArmPacket());
                            mcClientConn.getClient().getSession().send(new ClientPlayerActionPacket(PlayerAction.FINISH_DIGGING, new Position(selectedBlock.x, selectedBlock.y, selectedBlock.z), Face.TOP));
                        } else {
                            this.getWorld().removeBlock(this.getSelectedBlock().x, this.getSelectedBlock().y, this.getSelectedBlock().z);
                        }
                        updateSelectedBlock();
                    }
                } catch (CoordinatesOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            }
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

            float deltaX = 0;
            float deltaZ = 0;
            float yawSine = MathUtils.sinDeg(player.getRotation().getYaw());
            float yawCosine = MathUtils.cosDeg(player.getRotation().getYaw());
            deltaX += yawSine * moveTouchpad.getKnobPercentY() * posMult;
            deltaZ += -yawCosine * moveTouchpad.getKnobPercentY() * posMult;
            deltaX += yawCosine * moveTouchpad.getKnobPercentX() * posMult;
            deltaZ += yawSine * moveTouchpad.getKnobPercentX() * posMult;

            if(!movementHandler.checkDeltaCollision(player, deltaX, 0, deltaZ)
                    && !movementHandler.checkDeltaCollision(player, deltaX, 0, deltaZ)) {
                player.getPosition().offset(deltaX, 0, deltaZ);
                gameRenderer.calculateFrustum();
            }
        }
    }

    public boolean onAndroid() { return android; }

    public PerspectiveCamera getCamera() { return camera; }

    public OrthographicCamera getHudCamera() { return hudCamera; }

    public MovementHandler getMovementHandler() { return movementHandler; }

    public IngameHUD getHud() { return this.hud; }

    public ChatGUI getChatGUI() { return this.chatGUI; }

    public GameRenderer getGameRenderer() { return this.gameRenderer; }

    public boolean isWireframe() { return wireframe; }
    public void setWireframe(boolean wireframe) { this.wireframe = wireframe; }

    // Debug text that shows in the ingame HUD
    public boolean debugInfoEnabled() { return debugText; }
    public void setDebugInfo(boolean debugText) { this.debugText = debugText; }

}
