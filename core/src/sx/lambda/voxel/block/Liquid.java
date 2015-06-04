package sx.lambda.voxel.block;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.world.chunk.IChunk;

public class Liquid extends Block {

    private static final int MAX_META = 15;

    private static IBlockRenderer liquidRenderer;

    public Liquid(int id, String humanName, String textureLocation, int lightValue) {
        super(id, humanName,
                liquidRenderer == null ? (liquidRenderer = new MetadataHeightRenderer(MAX_META, true)) : liquidRenderer,
                new String[]{textureLocation}, true, false, true, false, true, true, lightValue);
    }

    @Override
    public BoundingBox calculateBoundingBox(IChunk c, int x, int y, int z) {
        short metadata = c.getMeta(x, y, z);

        return new BoundingBox(new Vector3(c.getStartPosition().x+x, y, c.getStartPosition().z+z),
                new Vector3(c.getStartPosition().x+x+1, y+getHeight(metadata), c.getStartPosition().z+z+1));
    }

    private float getHeight(short meta) {
        return 1-((meta+1)/(MAX_META+1));
    }

}
