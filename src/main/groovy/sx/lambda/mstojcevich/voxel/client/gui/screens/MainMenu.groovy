package sx.lambda.mstojcevich.voxel.client.gui.screens

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.client.gui.VboBufferedGuiScreen

import java.awt.Rectangle;

@CompileStatic
public class MainMenu extends VboBufferedGuiScreen {

    private final MainMenuButton[] buttons

    public MainMenu() {
    }

    private static class MainMenuButton {
        private final String title
        private final Closure onClick
        private final MainMenu parent
        private final Rectangle bounds

        MainMenuButton(MainMenu parent, String title, Closure onClick, int size) {
            this.parent = parent
            this.title = title
            this.onClick = onClick
            this.bounds = new Rectangle(size, size)
        }

        public Rectangle getBounds() {
            return this.bounds
        }

        public void click() {
            onClick()
        }

        public void setSize(int size) {
            this.bounds.setSize(size, size)
        }
    }
}
