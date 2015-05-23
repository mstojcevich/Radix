package sx.lambda.voxel.client.gui.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.client.gui.GuiScreen;

import static com.badlogic.gdx.graphics.GL20.*;

/**
 * Transition animation that slides the current screen up
 */
public class SlideUpAnimation extends TimedTransitionAnimation {

    private final GuiScreen currentScreen;

    private FrameBuffer fbo;
    private Texture fboTex;

    private OrthographicCamera cam;

    /**
     * @param currentScreen The currently shown screen
     * @param length        Time in milliseconds to run the animation for
     */
    public SlideUpAnimation(GuiScreen currentScreen, int length) {
        super(length);
        this.currentScreen = currentScreen;
    }

    @Override
    public void init() {
        super.init();
        fbo = new FrameBuffer(Pixmap.Format.RGBA4444, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        fboTex = fbo.getColorBufferTexture();
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL_BLEND);
        SpriteBatch batch = new SpriteBatch();
        batch.setProjectionMatrix(VoxelGameClient.getInstance().getHudCamera().combined);
        fbo.bind();
        batch.begin();
        currentScreen.render(false, batch);
        batch.end();
        FrameBuffer.unbind();
    }

    @Override
    public void render(SpriteBatch guiBatch) {
        float percentageDone = (float) getTimeSinceStart() / (float) getLength();

        guiBatch.draw(fboTex, 0, Gdx.graphics.getHeight() + Gdx.graphics.getHeight()*percentageDone, Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
    }

    @Override
    public void finish() {
        fbo.dispose();
        fboTex.dispose();
    }

}
