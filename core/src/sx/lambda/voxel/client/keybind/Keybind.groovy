package sx.lambda.voxel.client.keybind

import groovy.transform.CompileStatic

@CompileStatic
class Keybind {

    private final String id, displayName
    private final int key
    private final Runnable onPress
    private final boolean repeat

    /**
     * @param id Name to use in config files. Make it specific enough to avoid overlap.
     * @param displayName Name to use in GUIs for the bind
     * @param key LWJGL key code for the bind
     * @param onPress
     */
    public Keybind(String id, String displayName, int key, Runnable onPress) {
        this(id, displayName, key, false, onPress)
    }

    public Keybind(String id, String displayName, int key, boolean repeat, Runnable onPress) {
        this.id = id
        this.displayName = displayName
        this.key =  key
        this.onPress = onPress
        this.repeat = repeat
    }

    public void press() {
        onPress.run()
    }

    public String getName() { name }
    public int getKey() { key }
    public boolean shouldRepeat() { repeat }

}
