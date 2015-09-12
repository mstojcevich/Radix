package sx.lambda.voxel.client.gui.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.client.gui.GuiScreen;
import sx.lambda.voxel.settings.configs.VisualSettings;

public class VisualSettingsMenu implements GuiScreen {

    private Stage stage;

    public VisualSettingsMenu() {

    }

    @Override
    public void init() {
        VisualSettings visualSettings = RadixClient.getInstance().getSettingsManager().getVisualSettings();

        stage = new Stage();

        Table table = new Table();
        table.setFillParent(true);
        table.add(visualSettings.getFancyTrees().getManipulationActor());
        table.add(visualSettings.getPerCornerLight().getManipulationActor());
        table.row();
        table.add(visualSettings.getNonContinuous().getManipulationActor());
        table.add(visualSettings.getFinishEachFrame().getManipulationActor());
        stage.addActor(table);
    }

    @Override
    public void render(boolean inGame, SpriteBatch guiBatch) {
        stage.draw();
    }

    @Override
    public void finish() {
        stage.dispose();
    }

    @Override
    public void onMouseClick(int button, boolean up) {
        if(up) {
            stage.touchUp(mouseX, mouseY, 0, button);
        } else {
            stage.touchDown(mouseX, mouseY, 0, button);
        }
    }

    int mouseX, mouseY;
    @Override
    public void mouseMoved(int x, int y) {
        mouseX = x; mouseY = y;
        stage.mouseMoved(x, y);
    }

    @Override
    public void keyTyped(char c) {
        stage.keyTyped(c);
    }

}
