package sx.lambda.voxel.block;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.item.Tool;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

public class Liquid extends Block {

    private static final int MAX_META = 15;

    private static BlockRenderer liquidRenderer;

    Liquid(int id, String humanName, BlockRenderer renderer, String[] textureLocations, boolean translucent, boolean solid, boolean lightPassthrough, boolean selectable, boolean occludeCovered, boolean decreaseLight, boolean greedyMerge, int lightValue, float hardness, Tool.ToolMaterial requiredMaterial, Tool.ToolType requiredType) {
        super(id, humanName, renderer, textureLocations, translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge, lightValue, hardness, requiredMaterial, requiredType);
    }

    @Override
    public BoundingBox calculateBoundingBox(IChunk c, int x, int y, int z) {
        short metadata = 0;
        try {
            metadata = c.getMeta(x, y, z);
        } catch (CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }

        return new BoundingBox(new Vector3(c.getStartPosition().x+x, y, c.getStartPosition().z+z),
                new Vector3(c.getStartPosition().x+x+1, y+getHeight(metadata), c.getStartPosition().z+z+1));
    }

    private float getHeight(short meta) {
        return 1-((meta+1)/(MAX_META+1));
    }

}
