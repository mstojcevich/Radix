package sx.lambda.voxel.client.keybind;

public class Keybind {

    private final String id;
    private final String displayName;
    private final int key;
    private final Runnable onPress;

    public Keybind(String id, String displayName, int key, Runnable onPress) {
        this.id = id;
        this.displayName = displayName;
        this.key = key;
        this.onPress = onPress;
    }

    public void press() {
        onPress.run();
    }

    public String getName() {
        return displayName;
    }

    public int getKey() {
        return key;
    }

}
