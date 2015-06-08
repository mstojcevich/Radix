package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.client.gui.GuiScreen;

public class MainMenu implements GuiScreen {

    private static final int TARGET_BUTTON_SIZE = 150;
    private static final int BUTTON_SPACING = 4;

    private final MainMenuButton[] buttons;
    private boolean initialized;
    private SpriteBatch batch;

    private FrameBuffer mmPrerenderFbo;
    private Texture mmPrerender;

    private BitmapFont buttonFont;

    private Texture mmButtonTexture;

    private OrthographicCamera camera;

    public MainMenu() {
        buttons = new MainMenuButton[] {
                new MainMenuButton("Multiplayer (Local)", this::enterMPLocal, TARGET_BUTTON_SIZE),
                new MainMenuButton("Multiplayer (Lambda)", this::enterMPLambda, TARGET_BUTTON_SIZE),
                new MainMenuButton("Settings", () -> System.out.println("Settings pressed!"), TARGET_BUTTON_SIZE),
                new MainMenuButton("Quit Game", () -> VoxelGameClient.getInstance().startShutdown(), TARGET_BUTTON_SIZE),
        };
    }

    private void enterMPLocal() {
        System.out.println("Entering local mp");
        VoxelGameClient.getInstance().enterRemoteWorld("127.0.0.1", (short) 25565);
    }

    private void enterMPLambda() {
        System.out.println("Entering lambda mp");
        VoxelGameClient.getInstance().enterRemoteWorld("mc.stoj.pw", (short) 31173);
    }

    private void resize() {
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        if (mmPrerenderFbo != null)
            mmPrerenderFbo.dispose();
        mmPrerenderFbo = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        mmPrerender = mmPrerenderFbo.getColorBufferTexture();

        int dWidth = Gdx.graphics.getWidth();
        int buttonsPerRow = dWidth / (TARGET_BUTTON_SIZE + BUTTON_SPACING);

        int currentButtonNum = 0;
        for (MainMenuButton button : buttons) {
            int btnX = BUTTON_SPACING + (currentButtonNum % buttonsPerRow) * (TARGET_BUTTON_SIZE + BUTTON_SPACING);
            int btnY = Gdx.graphics.getHeight() - (BUTTON_SPACING + ((int) (currentButtonNum / buttonsPerRow) + 1) * (TARGET_BUTTON_SIZE + BUTTON_SPACING));
            button.setPosition(btnX, btnY);
            currentButtonNum++;
        }
    }

    public void init() {
        if (!initialized) {
            camera = new OrthographicCamera();
            batch = new SpriteBatch();
            batch.setTransformMatrix(VoxelGameClient.getInstance().getHudCamera().combined);
            batch.setTransformMatrix(camera.combined);
            mmButtonTexture = new Texture(Gdx.files.internal("textures/gui/mmBtn.png"));

            buttonFont = new BitmapFont();

            resize();
            rerender();

            initialized = true;
        }
    }

    protected void rerender() {
        mmPrerenderFbo.begin();
        batch.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        for (MainMenuButton button : buttons) {
            button.drawLabel();
            button.render();
        }
        batch.end();
        mmPrerenderFbo.end();
    }

    @Override
    public void render(boolean ingame, SpriteBatch guiBatch) {
        guiBatch.draw(mmPrerender, 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
    }

    @Override
    public void onMouseClick(int clickType) {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (clickType == 0) {
            for (MainMenuButton b : buttons) {
                if (b.bounds.contains(mouseX, mouseY)) {
                    b.click();
                }
            }
        }
    }

    @Override
    public void keyTyped(char c) {

    }


    @Override
    public void finish() {
        if (mmPrerenderFbo != null)
            mmPrerenderFbo.dispose();
        if (batch != null)
            batch.dispose();

        initialized = false;
    }

    class MainMenuButton {
        private final String title;
        private final Runnable onClick;
        private final Rectangle bounds;

        private GlyphLayout labelLayout;

        MainMenuButton(String title, Runnable onClick, int size) {
            this.title = title;
            this.onClick = onClick;
            this.bounds = new Rectangle(0, 0, size, size);
        }

        Rectangle getBounds() {
            return this.bounds;
        }

        void click() {
            onClick.run();
        }

        void setSize(int size) {
            this.bounds.setSize(size, size);
            if (buttonFont != null) {
                rerenderLabel();
            }
        }

        void setPosition(int x, int y) {
            this.bounds.setPosition(x, y);
            if (buttonFont != null) {
                rerenderLabel();
            }
        }

        private void render() {
            batch.draw(mmButtonTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        void drawLabel() {
            if (buttonFont != null) {
                if (labelLayout == null) {
                    labelLayout = buttonFont.draw(batch, title, -1000, -1000);
                }

                int textStartX = (int)(bounds.x + (bounds.width / 2.0f - labelLayout.width / 2.0f));
                int textStartY = (int)(bounds.y + (bounds.height / 2.0f + labelLayout.height / 2.0f));
                buttonFont.draw(batch, title, textStartX, textStartY);
            }
        }

        void rerenderLabel() {
            labelLayout = null; // force redraw next frame
        }
    }
}