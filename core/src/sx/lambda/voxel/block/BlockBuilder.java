package sx.lambda.voxel.block;

import sx.lambda.voxel.item.Tool.ToolMaterial;
import sx.lambda.voxel.item.Tool.ToolType;

public class BlockBuilder {

    private static IBlockRenderer defaultRenderer;

    private String humanName = "Undefined";
    private String[] textureLocations = new String[]{"textures/block/undefined.png"};
    private int id = -1;
    private boolean translucent = false;
    private IBlockRenderer renderer;
    private boolean solid = true;
    private boolean lightPassthrough = false;
    private boolean selectable = true;
    private boolean occludeCovered = true;
    private boolean decreaseLight = true;
    private boolean greedyMerge = true;
    private int lightValue = 0;
    private float hardness = 0;
    private ToolType requiredToolType = ToolType.THESE_HANDS;
    private ToolMaterial requiredToolMaterial = ToolMaterial.THESE_HANDS;

    /**
     * Set the display name for the block
     *
     * Defaults to "Undefined"
     */
    public BlockBuilder setHumanName(String hn) {
        this.humanName = hn;
        return this;
    }

    /**
     * Set the location to the texture for the block
     *
     * Defaults to "textures/block/undefined.png"
     * @param tl Location, relative to the assets directory
     */
    public BlockBuilder setTextureLocation(String tl) {
        this.textureLocations[0] = tl;
        return this;
    }

    /**
     * Set the texture locations for a multi-texture block
     *
     * @param tls List of locations relative to the assets directory
     */
    public BlockBuilder setTextureLocations(String ... tls) {
        this.textureLocations = tls;
        return this;
    }

    /**
     * Set the ID for the block. Must be unique.
     *
     * A MissingElementException will be thrown if you attempt to build without an id.
     */
    public BlockBuilder setID(int id) {
        this.id = id;
        return this;
    }

    /**
     * Sets whether the block contains any transparent elements
     *
     * Defaults to false
     */
    public BlockBuilder setTranslucent(boolean translucent) {
        this.translucent = translucent;
        return this;
    }

    /**
     * Set whether the block can be clipped through
     *
     * Defaults to true
     */
    public BlockBuilder setSolid(boolean solid) {
        this.solid = solid;
        return this;
    }

    /**
     * Set whether light passes through the block (as if it was air)
     *
     * Defaults to false
     */
    public BlockBuilder setLightPassthrough(boolean passthrough) {
        this.lightPassthrough = passthrough;
        return this;
    }

    /**
     * Set whether the block is selectable in the world to be broken or placed on
     *
     * Defaults to true
     */
    public BlockBuilder setSelectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    /**
     * Set whether to occlude (hide) sides that are blocked by other blocks.
     * This would typically be set to false on blocks like tall grass where sides can be seen even if "covered".
     *
     * This is implied false for translucent blocks only when checking against non-translucent blocks.
     *
     * Defaults to true
     */
    public BlockBuilder setOccludeCovered(boolean occlude) {
        this.occludeCovered = occlude;
        return this;
    }

    /**
     * Set whether to decrease light when passing down onto air.
     * Setting to false creates the Minecraft-like effect of being able to see other leaves through leaves but still casting a shadow.
     *
     * Defaults to true
     */
    public BlockBuilder setDecreaseLight(boolean decreaseLight) {
        this.decreaseLight = decreaseLight;
        return this;
    }

    /**
     * Set the light value of the block for emitting light
     *
     * Defaults to 0
     *
     * @param lightValue Integer between 0 and 15 representing the light emission value of the block
     */
    public BlockBuilder setLightValue(int lightValue) {
        this.lightValue = lightValue;
        return this;
    }

    public BlockBuilder dontMerge() {
        this.greedyMerge = false;
        return this;
    }

    public BlockBuilder setHardness(float hardness) {
        this.hardness = hardness;
        return this;
    }

    public BlockBuilder setRequiredToolType(ToolType requiredType) {
        this.requiredToolType = requiredType;
        return this;
    }

    public BlockBuilder setRequiredToolMaterial(ToolMaterial requiredMaterial) {
        this.requiredToolMaterial = requiredMaterial;
        return this;
    }

    public BlockBuilder setBlockRenderer(IBlockRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Block build() throws MissingElementException {
        if (id == -1) throw new MissingElementException("id");
        if (renderer == null) {
            if(defaultRenderer == null) {
                defaultRenderer = new NormalBlockRenderer();
            }
            renderer = defaultRenderer;
        }
        return new Block(id, humanName, renderer, textureLocations, translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge, lightValue, hardness, requiredToolMaterial, requiredToolType);
    }

    public class MissingElementException extends Exception {
        public MissingElementException(String missingEl) {
            super("You cannot create a block without " + missingEl);
        }
    }

}
