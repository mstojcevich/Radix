package sx.lambda.voxel.settings

import groovy.transform.CompileStatic
import sx.lambda.voxel.settings.configs.VisualSettings

@CompileStatic
class SettingsManager {

    private final VisualSettings visualSettings

    public SettingsManager() {
        visualSettings = new VisualSettings()
    }

    public VisualSettings getVisualSettings() { visualSettings }

}
