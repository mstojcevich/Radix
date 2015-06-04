package sx.lambda.voxel.block;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.world.chunk.IChunk;

public class Snow extends Block {

    private static final float MAX_META = 7;

    public Snow() {
        super(BuiltInBlockIds.SNOW_ID, "Snow", new MetadataHeightRenderer(7), new String[]{"textures/block/snow.png"},
                false, true, true, true, false, true, true, 0);
    }

    @Override
    public BoundingBox calculateBoundingBox(IChunk c, int x, int y, int z) {
        short metadata = c.getMeta(x, y, z);

        return new BoundingBox(new Vector3(c.getStartPosition().x+x, y, c.getStartPosition().z+z),
                new Vector3(c.getStartPosition().x+x+1, y+getHeight(metadata), c.getStartPosition().z+z+1));
    }

    private float getHeight(short meta) {
        return (meta+1)/(MAX_META+1);
    }

}
