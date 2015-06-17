package sx.lambda.voxel.block;

import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.item.Tool.*;

/**
 * Block class used for serialization of blocks
 *
 * @see sx.lambda.voxel.block.BlockBuilder for details on what all of the fields mean
 */
public class JsonBlock {

    private final String humanName;
    private final int id;
    private final boolean translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge;
    private final String[] textureLocations;
    private final int lightValue;
    private final float hardness;
    private final ToolMaterial requiredMaterial;
    private final ToolType requiredType;

    /**
     * Fully qualified name of a custom class to use when creating the block.
     *
     * Should extend Block and have the same constructor that Block has.
     *
     * ex. "sx.lambda.voxel.block.Snow"
     */
    private final String customClass;

    /**
     * Unique ID of the renderer for the block.
     *
     * Should be one of the registered renderers
     *  (see the default ones in sx.lambda.voxel.api.RadixApi.registerDefaultBlockRenderers())
     *  or a custom defined renderer that was registered with sx.lambda.voxel.api.RadixApi.registerBlockRenderer
     *  before the block load phase.
     */
    private final String renderer;

    public JsonBlock() {
        humanName = "Undefined";
        id = -1;
        textureLocations = new String[]{ "textures/block/undefined.png" };
        renderer = "Builtin.NORMAL";
        translucent = lightPassthrough = false;
        solid = selectable = occludeCovered = decreaseLight = greedyMerge = true;
        lightValue = 0;
        hardness = 0;
        requiredMaterial = ToolMaterial.THESE_HANDS;
        requiredType = ToolType.THESE_HANDS;
        customClass = null;
    }

    /**
     * Creates a JsonBlock from the given block.
     */
    public JsonBlock(Block b) {
        humanName = b.getHumanName();
        id = b.getID();
        textureLocations = b.getTextureLocations();
        renderer = b.getRenderer().getUniqueID();
        hardness = b.getHardness();
        requiredMaterial = b.getRequiredToolMaterial();
        requiredType = b.getRequiredToolType();
        translucent = b.isTranslucent();
        solid = b.isSolid();
        lightPassthrough = b.doesLightPassThrough();
        selectable = b.isSelectable();
        occludeCovered = b.occludeCovered();
        decreaseLight = b.decreasesLight();
        greedyMerge = b.shouldGreedyMerge();
        lightValue = b.getLightValue();
        customClass = b.getClass().getName();
    }

    /**
     * Create a block out of the data in the JsonBlock.
     *
     * Should only be called after the BlockRenderer registration phase.
     *
     * @return newly created Block instance based on the data in this object
     *
     * @throws sx.lambda.voxel.api.RadixAPI.NoSuchRendererException The specified renderer unique ID does not exist or is not yet registered.
     *      Could be caused by calling this before the BlockRenderer registration phase or registering your BlockRenderer too late or not at all.
     * @throws sx.lambda.voxel.block.BlockBuilder.MissingElementException One of the required elements is missing from the block data.
     * @throws sx.lambda.voxel.block.BlockBuilder.CustomClassException The custom class that was defined was invalid for some reason.
     */
    public Block createBlock() throws RadixAPI.NoSuchRendererException, BlockBuilder.MissingElementException, BlockBuilder.CustomClassException {
        BlockRenderer renderer = RadixAPI.instance.getBlockRenderer(this.renderer);

        BlockBuilder builder = new BlockBuilder()
                .setHumanName(humanName).setID(id).setTextureLocations(textureLocations).setRenderer(renderer)
                .setHardness(hardness).setRequiredToolMaterial(requiredMaterial).setRequiredToolType(requiredType)
                .setTranslucent(translucent).setSolid(solid).setLightPassthrough(lightPassthrough).setOccludeCovered(occludeCovered)
                .setDecreaseLight(decreaseLight).setSelectable(selectable).setLightValue(lightValue).setCustomClass(customClass);
        if(!greedyMerge)
            builder.dontMerge();

        try {
            return builder.build();
        } catch (BlockBuilder.MissingElementException ex) {
            System.err.printf("MISSING ELEMENT FOR JSON BLOCK \"%s\". THIS SHOULD NEVER HAPPEN!\n", humanName);
            ex.printStackTrace();
            return null;
        }
    }

}
