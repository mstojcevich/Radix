package sx.lambda.voxel.client.gui.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.client.gui.GuiScreen;
import sx.lambda.voxel.util.gl.FrameBuffer;

import static com.badlogic.gdx.graphics.GL20.*;

/**
 * Transition animation that slides the current screen up
 */
public class SlideUpAnimation extends TimedTransitionAnimation {

    private final GuiScreen currentScreen;

    private FrameBuffer fbo;

    private OrthographicCamera cam;

    /**
     * @param currentScreen The currently shown screen
     * @param length Time in milliseconds to run the animation for
     */
    public SlideUpAnimation(GuiScreen currentScreen, int length) {
        super(length);
        this.currentScreen = currentScreen;
    }

    @Override
    public void init() {
        super.init();
        fbo = new FrameBuffer();
        VoxelGameClient.getInstance().enableGuiShader();
        fbo.bind();
        // TODO convert to use libgdx's prepared matrices (in camera??)
        cam = new OrthographicCamera();
        cam.update();
        // TODO apply cam
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL_BLEND);
        SpriteBatch batch = new SpriteBatch();
        batch.begin();
        currentScreen.render(false, batch);
        batch.end();
        fbo.unbind();
    }

    @Override
    public void render() {
        VoxelGameClient.getInstance().getGuiShader().enableTexturing();
        float percentageDone = (float)getTimeSinceStart() / (float)getLength();
        fbo.drawTexture(VoxelGameClient.getInstance().getTextureManager(), 0, (int)(Gdx.graphics.getWidth()*percentageDone), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), VoxelGameClient.getInstance().getGuiShader().getPositionAttrib(), VoxelGameClient.getInstance().getGuiShader().getTexCoordAttrib());
    }

    @Override
    public void finish() {
        fbo.cleanup();
    }

}
