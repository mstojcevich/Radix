package sx.lambda.voxel.block;

public class BlockBuilder {

    private String humanName = "Undefined";
    private String textureLocation = "textures/block/undefined.png";
    private int id = -1;
    private boolean translucent = false;
    private IBlockRenderer renderer;
    private boolean solid = true;
    private boolean lightPassthrough = false;

    public BlockBuilder setHumanName(String hn) {
        this.humanName = hn;
        return this;
    }

    public BlockBuilder setTextureLocation(String tl) {
        this.textureLocation = tl;
        return this;
    }

    public BlockBuilder setID(int id) {
        this.id = id;
        return this;
    }

    public BlockBuilder setTranslucent(boolean translucent) {
        this.translucent = translucent;
        return this;
    }

    public BlockBuilder setSolid(boolean solid) {
        this.solid = solid;
        return this;
    }

    /**
     * Set whether light passes through the block (as if it was air)
     */
    public BlockBuilder setLightPassthrough(boolean passthrough) {
        this.lightPassthrough = passthrough;
        return this;
    }

    public BlockBuilder setBlockRenderer(IBlockRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Block build() throws MissingElementException {
        if (id == -1) throw new MissingElementException("id");
        if (renderer == null) {
            renderer = new NormalBlockRenderer(id);
        }
        return new Block(id, humanName, renderer, textureLocation, translucent, solid, lightPassthrough);
    }

    public class MissingElementException extends Exception {
        public MissingElementException(String missingEl) {
            super("You cannot create a block without " + missingEl);
        }
    }

}
