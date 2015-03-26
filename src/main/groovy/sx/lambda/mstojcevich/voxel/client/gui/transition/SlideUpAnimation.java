package sx.lambda.mstojcevich.voxel.client.gui.transition;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.client.gui.GuiScreen;
import sx.lambda.mstojcevich.voxel.util.gl.FrameBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Transition animation that slides the current screen up
 */
public class SlideUpAnimation extends TimedTransitionAnimation {

    private final GuiScreen currentScreen;

    private FrameBuffer fbo;

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
        VoxelGame.getInstance().enableGuiShader();
        fbo.bind();
        GL11.glMatrixMode(GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -1, 1);
        GL11.glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glClearColor(1, 1, 1, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_BLEND);
        currentScreen.render(false);
        fbo.unbind();
    }

    @Override
    public void render() {
        VoxelGame.getInstance().getGuiShader().disableColors();
        VoxelGame.getInstance().getGuiShader().enableTexturing();
        float percentageDone = (float)getTimeSinceStart() / (float)getLength();
        fbo.drawTexture(VoxelGame.getInstance().getTextureManager(), 0, (int)(Display.getWidth()*percentageDone), Display.getWidth(), Display.getHeight(), VoxelGame.getInstance().getGuiShader().getPositionAttrib(), VoxelGame.getInstance().getGuiShader().getTexCoordAttrib());
    }

    @Override
    public void finish() {
        fbo.cleanup();
    }

}
