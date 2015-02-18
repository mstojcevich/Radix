package sx.lambda.mstojcevich.voxel.settings

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.settings.configs.VisualSettings

@CompileStatic
class SettingsManager {

    private final VisualSettings visualSettings

    public SettingsManager() {
        visualSettings = new VisualSettings()
    }

    public VisualSettings getVisualSettings() { visualSettings }

}
