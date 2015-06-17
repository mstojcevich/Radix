package sx.lambda.voxel.block;

import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.item.Tool.ToolMaterial;
import sx.lambda.voxel.item.Tool.ToolType;

import java.lang.reflect.Constructor;

public class BlockBuilder {

    private String humanName = "Undefined";
    private String[] textureLocations = new String[]{"textures/block/undefined.png"};
    private int id = -1;
    private boolean translucent = false;
    private BlockRenderer renderer;
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
    private String customClass = null;

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

    public BlockBuilder setRenderer(BlockRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    /**
     * Set the custom class to be used when creating the block
     * @param clazz Fully qualified class name of the custom class for the block.
     *              The class specified must extends Block and use the constructor that Block has.
     */
    public BlockBuilder setCustomClass(String clazz) {
        this.customClass = clazz;
        return this;
    }

    /**
     * @throws MissingElementException A required element of the block was missing.
     *         Usually thrown if an ID is not set.
     * @throws CustomClassException An error occurred when trying to create an instance of the custom class.
     */
    public Block build() throws MissingElementException, CustomClassException {
        if (id == -1) throw new MissingElementException("id");
        if (renderer == null) {
            try {
                renderer = RadixAPI.instance.getBlockRenderer("Builtin.NORMAL");
            } catch (RadixAPI.NoSuchRendererException ex) {
                System.err.printf("Block \"%s\" was created before the normal block renderer was registered.\n" +
                                "This usually means that the block was created during the wrong event or startup stage.\n" +
                                "The block will still be registered, but a new instance of NormalBlockRenderer will be made, which is undesirable.\n",
                        humanName);
                ex.printStackTrace();

                renderer = new NormalBlockRenderer();
            }
        }

        if(customClass == null) {
            return new Block(id, humanName, renderer, textureLocations, translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge, lightValue, hardness, requiredToolMaterial, requiredToolType);
        } else {
            // Use reflection to invoke the constructor of the custom class specified

            try {
                Class cl = Class.forName(customClass);
                try {
                    Constructor co = cl.getDeclaredConstructor(int.class, String.class, BlockRenderer.class, String[].class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, int.class, float.class, ToolMaterial.class, ToolType.class);
                    co.setAccessible(true);
                    return (Block)co.newInstance(id, humanName, renderer, textureLocations, translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge, lightValue, hardness, requiredToolMaterial, requiredToolType);
                } catch(Exception ex) {
                    ex.printStackTrace();
                    throw new CustomClassException(
                            String.format(
                                    "Failed to find the primary Block constructor in the custom class for block \"%s\" with id %d. Make sure you have it. Also make sure your class extends Block.",
                                    humanName, id
                            ));
                }
            } catch (ClassNotFoundException e) {
                throw new CustomClassException(
                        String.format(
                                "Failed to find the custom class for block \"%s\" with id %d.\n" +
                                        "Make sure the class at \"%s\" exists.",
                                humanName, id, customClass
                        ));
            }
        }
    }

    public class MissingElementException extends Exception {
        public MissingElementException(String missingEl) {
            super("You cannot create a block without " + missingEl);
        }
    }

    public class CustomClassException extends Exception {
        public CustomClassException(String reason) {
            super(reason);
        }
    }

}
