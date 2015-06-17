package sx.lambda.voxel.block;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.item.Item;
import sx.lambda.voxel.item.Tool;
import sx.lambda.voxel.item.Tool.ToolMaterial;
import sx.lambda.voxel.item.Tool.ToolType;
import sx.lambda.voxel.world.chunk.IChunk;

public class Block extends Item {

    private final transient BlockRenderer renderer;
    private boolean translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge;
    private final String[] textureLocations;
    private int textureIndex;
    private int lightValue;
    private final float hardness;
    private final ToolMaterial requiredMaterial;
    private final ToolType requiredType;

    /**
     * DO NOT USE THIS CONSTRUCTOR, USE BLOCKBUILDER INSTEAD
     */
    Block(int id, String humanName, BlockRenderer renderer, String textureLocations[],
          boolean translucent, boolean solid, boolean lightPassthrough, boolean selectable, boolean occludeCovered,
          boolean decreaseLight, boolean greedyMerge,
          int lightValue, float hardness, ToolMaterial requiredMaterial, ToolType requiredType) {
        super(id, humanName);
        this.renderer = renderer;
        this.translucent = translucent;
        this.solid = solid;
        this.textureLocations = textureLocations;
        this.lightPassthrough = lightPassthrough;
        this.selectable = selectable;
        this.occludeCovered = occludeCovered;
        this.decreaseLight = decreaseLight;
        this.lightValue = lightValue;
        this.greedyMerge = greedyMerge;
        this.hardness = hardness;
        this.requiredMaterial = requiredMaterial;
        this.requiredType = requiredType;
    }

    public BlockRenderer getRenderer() {
        return this.renderer;
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

    public float getHardness() {
        return this.hardness;
    }

    public ToolMaterial getRequiredToolMaterial() {
        return this.requiredMaterial;
    }

    public ToolType getRequiredToolType() {
        return this.requiredType;
    }

    /**
     * Get the time it takes to break the block with the given tool
     * @return Length, in milliseconds, to break the block
     */
    public int getBreakTimeMS(Tool tool) {
        if(hardness > 100 || hardness < 0) {
            return Integer.MAX_VALUE;
        }

        ToolType tType = ToolType.THESE_HANDS;
        ToolMaterial tMaterial = ToolMaterial.THESE_HANDS;
        if(tool != null) {
            tType = tool.getType();
            tMaterial = tool.getMaterial();
        }

        float timeSeconds = hardness * 1.5f;
        if((tType != requiredType && requiredType != ToolType.THESE_HANDS && requiredMaterial != ToolMaterial.THESE_HANDS)
                || (tMaterial.materialStrength < requiredMaterial.materialStrength)) {
            timeSeconds *= 3.33;
        } else if(tMaterial.materialStrength >= requiredMaterial.materialStrength) {
            timeSeconds /= requiredMaterial.speedMult;
        }

        return (int)(timeSeconds * 1000f);
    }

}
