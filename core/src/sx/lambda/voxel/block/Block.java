package sx.lambda.voxel.block;

public class Block {

    // AIR(0), DIRT(1), GRASS(2), STONE(3), WATER(4, new WaterRenderer(4), true, false), SAND(5);

    private int id;
    private final transient IBlockRenderer renderer;
    private final boolean translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight;
    private final String humanName;
    private final String[] textureLocations;
    private int textureIndex;

    Block(int id, String humanName, IBlockRenderer renderer, String textureLocations[],
          boolean translucent, boolean solid, boolean lightPassthrough, boolean selectable, boolean occludeCovered, boolean decreaseLight) {
        this.id = id;
        this.renderer = renderer;
        this.translucent = translucent;
        this.solid = solid;
        this.humanName = humanName;
        this.textureLocations = textureLocations;
        this.lightPassthrough = lightPassthrough;
        this.selectable = selectable;
        this.occludeCovered = occludeCovered;
        this.decreaseLight = decreaseLight;
    }

    public IBlockRenderer getRenderer() {
        return this.renderer;
    }

    public int getID() {
        return this.id;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public boolean doesLightPassThrough() {
        return this.lightPassthrough;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getHumanName() {
        return this.humanName;
    }

    public String getTextureLocation() {
        return textureLocations[0];
    }

    public String[] getTextureLocations() {
        return textureLocations;
    }

    /**
     * Set the beginning index in the texture map for the texture set for the block
     *
     * THIS IS USED INTERNALLY BY THE TEXTURING SYSTEM
     * YOU PROBABLY DO NOT WANT TO CALL THIS
     *
     * @param index Number of the first texture of the block in the texture map
     */
    public void setTextureIndex(int index) {
        this.textureIndex = index;
    }

    /**
     * @return The index at which you can find the first texture of the block in the texture map
     */
    public int getTextureIndex() {
        return this.textureIndex;
    }

    /**
     * @return True if the block is selectable to be broken or placed on
     */
    public boolean isSelectable() {
        return this.selectable;
    }

    /**
     * @return True if covered sides of the block should be occluded when rendering
     */
    public boolean occludeCovered() {
        return occludeCovered;
    }

    /**
     * @return True if, when going down to another non-air block, light is decreased
     */
    public boolean decreasesLight() {
        return this.decreaseLight;
    }

}
