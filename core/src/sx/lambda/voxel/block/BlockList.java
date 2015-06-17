package sx.lambda.voxel.block;

import java.io.Serializable;

/**
 * Object used for serializing block list json files.
 */
public class BlockList implements Serializable {

    private final JsonBlock[] blocks;

    /**
     * Creates a new BlockList for the specified blocks
     *
     * You probably shouldn't call this because this class is only used for serialization
     */
    @Deprecated
    public BlockList(Block[] blocks) {
        this.blocks = new JsonBlock[blocks.length];

        for(int i = 0; i < blocks.length; i++) {
            Block b = blocks[i];
            if(b == null)
                continue;
            this.blocks[i] = new JsonBlock(b);
        }
    }

    /**
     * @return Array of blocks. Not in order by ID or anything special.
     */
    public JsonBlock[] getBlocks() {
        return blocks;
    }

}
