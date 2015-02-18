package sx.lambda.mstojcevich.voxel.client.gui

public interface GuiScreen {

    /**
     * Initializes in an opengl context
     */
    public void init()

    /**
     * Draws the screen
     * @param inGame Whether the screen is being drawn inside a world
     */
    public void render(boolean inGame)

}