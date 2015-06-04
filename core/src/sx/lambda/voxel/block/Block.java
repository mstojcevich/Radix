package sx.lambda.voxel.block;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.world.chunk.IChunk;

public class Block {

    private int id;
    private final transient IBlockRenderer renderer;
    private boolean translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge;
    private final String humanName;
    private final String[] textureLocations;
    private int textureIndex;
    private int lightValue;

    Block(int id, String humanName, IBlockRenderer renderer, String textureLocations[],
          boolean translucent, boolean solid, boolean lightPassthrough, boolean selectable, boolean occludeCovered,
          boolean decreaseLight, boolean greedyMerge,
          int lightValue) {
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
        this.lightValue = lightValue;
        this.greedyMerge = greedyMerge;
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

    /**
     * Get the light emission value of the block
     * @return Integer between 0 and 15 representing the light emission value of the block
     */
    public int getLightValue() {
        return this.lightValue;
    }

    public boolean shouldGreedyMerge() {
        return this.greedyMerge;
    }

    /**
     * Set whether you can select the block in the world
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * Set whether light should decrease when passing through this block to other blocks
     */
    public void setDecreaseLight(boolean decrease) {
        this.decreaseLight = decrease;
    }

    /**
     * Set whether light should always ignore this block
     */
    public void setLightPassthrough(boolean passthrough) {
        this.lightPassthrough = passthrough;
    }

    /**
     * Set whether the block can be walked through
     */
    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    /**
     * Set whether to consider the block translucent and to render it after opaque blocks
     */
    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
    }

    /**
     * Set whether to occlude neighbors that the block is covering
     */
    public void setOccludeCovered(boolean occludeCovered) {
        this.occludeCovered = occludeCovered;
    }

    /**
     * Get the bounding box of an instance of the block at the specified coordinates
     *
     * @param c Chunk the block is in
     * @param x Chunk-relative x position of the block. 0->(chunkWidth-1) inclusive.
     * @param y Chunk-relative y position of the block. 0->(chunkHeight-1) inclusive.
     * @param z Chunk-relative z position of the block. 0->(chunkWidth-1) inclusive.
     */
    public BoundingBox calculateBoundingBox(IChunk c, int x, int y, int z) {
        Vector3 corner1 = new Vector3(c.getStartPosition().x+x, c.getStartPosition().y+y, c.getStartPosition().z+z);
        Vector3 corner2 = corner1.cpy().add(1, 1, 1);
        return new BoundingBox(corner1, corner2);
    }

}
