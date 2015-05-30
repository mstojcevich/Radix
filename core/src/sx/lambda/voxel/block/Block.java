package sx.lambda.voxel.block;

public class Block {

    // AIR(0), DIRT(1), GRASS(2), STONE(3), WATER(4, new WaterRenderer(4), true, false), SAND(5);

    private int id;
    private final transient IBlockRenderer renderer;
    private final boolean transparent, solid, lightPassthrough;
    private final String humanName;
    private final String textureLocation;

    Block(int id, String humanName, IBlockRenderer renderer, String textureLocation, boolean transparent, boolean solid, boolean lightPassthrough) {
        this.id = id;
        this.renderer = renderer;
        this.transparent = transparent;
        this.solid = solid;
        this.humanName = humanName;
        this.textureLocation = textureLocation;
        this.lightPassthrough = lightPassthrough;
    }

    public IBlockRenderer getRenderer() {
        return this.renderer;
    }

    public int getID() {
        return this.id;
    }

    public boolean isTransparent() {
        return this.transparent;
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
        return textureLocation;
    }
}
