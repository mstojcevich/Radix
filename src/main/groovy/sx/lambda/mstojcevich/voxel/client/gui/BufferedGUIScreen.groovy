package sx.lambda.mstojcevich.voxel.client.gui

import org.lwjgl.opengl.GL11

class BufferedGUIScreen implements GuiScreen {

    protected int displayList
    private boolean initialized
    private boolean renderedBefore

    public BufferedGUIScreen() {

    }


    @Override
    void init() {
        displayList = GL11.glGenLists(1)
    }

    @Override
    void render(boolean inGame) {
        if(!renderedBefore) {
            rerender(true)
            renderedBefore = true //just in case someone overrode rerender and didn't callback
        } else {
            GL11.glCallList(displayList)
        }
    }

    @Override
    void finish() {
        GL11.glDeleteLists(displayList, 1)
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
