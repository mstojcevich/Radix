package sx.lambda.voxel.settings.options;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import sx.lambda.voxel.RadixClient;

public class ToggleOption implements Option<Boolean> {

    private final String title;
    private boolean value = false;

    /**
     * @param title Title of the toggle option
     * @param initialValue Value for the toggle option to start out as
     */
    public ToggleOption(String title, boolean initialValue) {
        this.title = title;
        this.value = initialValue;
    }

    /**
     * Creates a toggle option with an initial value of false (disabled)
     * @param title Title of the option
     */
    public ToggleOption(String title) {
        this(title, false);
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Actor getManipulationActor() {
        TextButton button = new TextButton(String.format("%s: %s", title, getValueStr()), RadixClient.getInstance().getSceneTheme().getButtonStyle());
        button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                ToggleOption.this.value = !ToggleOption.this.value;
                button.setText(String.format("%s: %s", title, getValueStr()));
            }
        });
        return button;
    }

    @Override
    public String getValueStr() {
        return value ? "Enabled" : "Disabled";
    }

    public void toggle() {
        this.value = !this.value;
    }

}
