package sx.lambda.voxel.settings;

import sx.lambda.voxel.settings.configs.VisualSettings;

public class SettingsManager {

    private final VisualSettings visualSettings;

    public SettingsManager() {
        visualSettings = new VisualSettings();
    }

    public VisualSettings getVisualSettings() {
        return visualSettings;
    }

}
