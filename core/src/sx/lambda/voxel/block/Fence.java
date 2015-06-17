package sx.lambda.voxel.block;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.item.Tool;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

public class Fence extends Block {

    private static final float WIDTH = 0.25f;
    private static final float R = WIDTH/2f;

    Fence(int id, String humanName, BlockRenderer renderer, String[] textureLocations, boolean translucent, boolean solid, boolean lightPassthrough, boolean selectable, boolean occludeCovered, boolean decreaseLight, boolean greedyMerge, int lightValue, float hardness, Tool.ToolMaterial requiredMaterial, Tool.ToolType requiredType) {
        super(id, humanName, renderer, textureLocations, translucent, solid, lightPassthrough, selectable, occludeCovered, decreaseLight, greedyMerge, lightValue, hardness, requiredMaterial, requiredType);
    }

    @Override
    public BoundingBox calculateBoundingBox(IChunk c, int x, int y, int z) {
        float x1 = c.getStartPosition().x+x+0.5f-R;
        float z1 = c.getStartPosition().z+z+0.5f-R;
        float x2 = x1+WIDTH;
        float z2 = z1+WIDTH;
        for(Side side : Side.values()) {
            if(side == Side.TOP || side == Side.BOTTOM)
                continue;
            int sx = x;
            int sz = z;
            IChunk sChunk = c;
            switch(side) {
                case EAST:
                    sx += 1;
                    break;
                case WEST:
                    sx -= 1;
                    break;
                case NORTH:
                    sz += 1;
                    break;
                case SOUTH:
                    sz -= 1;
                    break;
            }
            if (sz < 0) {
                sChunk = c.getWorld().getChunk(c.getStartPosition().x+sx, c.getStartPosition().z+sz);
                sz += c.getWorld().getChunkSize();
            } else if (sz > c.getWorld().getChunkSize() - 1) {
                sChunk = c.getWorld().getChunk(c.getStartPosition().x+sx, c.getStartPosition().z+sz);
                sz -= c.getWorld().getChunkSize();
            }
            if (sx < 0) {
                sChunk = c.getWorld().getChunk(c.getStartPosition().x+sx, c.getStartPosition().z+sz);
                sx += c.getWorld().getChunkSize();
            } else if (sx > c.getWorld().getChunkSize() - 1) {
                sChunk = c.getWorld().getChunk(c.getStartPosition().x+sx, c.getStartPosition().z+sz);
                sx -= c.getWorld().getChunkSize();
            }

            if (sChunk == null)
                continue;

            try {
                Block sBlock = sChunk.getBlock(sx, y, sz);
                if (sBlock != null) {
                    if (sBlock.isSolid()) {
                        switch (side) {
                            case EAST:
                                x2 = c.getStartPosition().x + x + 1;
                                break;
                            case WEST:
                                x1 = c.getStartPosition().x + x;
                                break;
                            case NORTH:
                                z2 = c.getStartPosition().z + z + 1;
                                break;
                            case SOUTH:
                                z1 = c.getStartPosition().z + z;
                                break;
                        }
                    }
                }
            } catch (CoordinatesOutOfBoundsException ex) {
                ex.printStackTrace();
            }
        }
        Vector3 corner1 = new Vector3(x1, c.getStartPosition().y+y, z1);
        Vector3 corner2 = new Vector3(x2, c.getStartPosition().y+y+1.5f, z2);
        return new BoundingBox(corner1, corner2);
    }

}
