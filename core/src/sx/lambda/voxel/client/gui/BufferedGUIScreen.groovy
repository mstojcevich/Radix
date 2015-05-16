package sx.lambda.voxel.client.gui

import com.badlogic.gdx.Gdx
import groovy.transform.CompileStatic

@CompileStatic
class BufferedGUIScreen implements GuiScreen {

    private boolean initialized
    private boolean renderedBefore

    public BufferedGUIScreen() {

    }


    @Override
    void init() {
    }

    @Override
    void render(boolean inGame) {
        if(!renderedBefore) {
            rerender(true)
            renderedBefore = true //just in case someone overrode rerender and didn't callback
        } else {
        }
    }

    @Override
    void finish() {
        initialized = false
        renderedBefore = false
    }

    @Override
    void onMouseClick(int button) {}

    /**
     * Rerenders to the display list
     * Must be ran in an opengl context
     * @param exec - Whether to execute the GL commands or just store in the list.
     *      if unsure, choose false
     */
    public void rerender(boolean exec) {
        if(!initialized) {
            this.init()
        }
        renderedBefore = true
    }

}
