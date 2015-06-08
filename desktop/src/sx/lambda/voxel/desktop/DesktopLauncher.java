package sx.lambda.voxel.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sx.lambda.voxel.RadixClient;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config;
        FileHandle glConfigFile = new FileHandle("conf/lwjgl.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if(!glConfigFile.exists()) {
            config = createDefaultConfig();
            glConfigFile.writeString(gson.toJson(config), false);
        } else {
            try {
                config = gson.fromJson(glConfigFile.readString(), LwjglApplicationConfiguration.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error reading lwjgl config! Using defaults.");
                config = createDefaultConfig();
            }
        }

		new LwjglApplication(new RadixClient(), config);
	}

    private static LwjglApplicationConfiguration createDefaultConfig() {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.useGL30 = true;
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.samples = 16;

        return config;
    }
}