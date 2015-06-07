package sx.lambda.voxel.client.render.meshing;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.IBlockRenderer;
import sx.lambda.voxel.block.NormalBlockRenderer;
import sx.lambda.voxel.block.Side;
import sx.lambda.voxel.world.chunk.BlockStorage;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

import java.util.ArrayList;
import java.util.List;


// TODO not meshing the chunk sides causes holes after the neighbor chunk is added.
public class GreedyMesher implements Mesher {

    private final IChunk chunk;
    private final boolean perCornerLight;

    /**
     * @param chunk Chunk to mesh
     * @param perCornerLight Whether to average light on a per-corner basis
     */
    public GreedyMesher(IChunk chunk, boolean perCornerLight) {
        this.chunk = chunk;
        this.perCornerLight = perCornerLight;
    }

    @Override
    public Mesh meshVoxels(MeshBuilder builder, BlockStorage storage, UseCondition condition) {
        List<Face> faces = getFaces(storage, condition);

        return meshFaces(faces, builder);
    }

    public List<Face> getFaces(BlockStorage storage, UseCondition condition, OccludeCondition ocCond, MergeCondition shouldMerge) {
        List<Face> faces = new ArrayList<>();

        // TODO don't allocate arrays for lightData if pcld is enabled

        PerCornerLightData bright = null;
        if(perCornerLight) {
            bright = new PerCornerLightData();
            bright.l00 = 1;
            bright.l01 = 1;
            bright.l10 = 1;
            bright.l11 = 1;
        }

        // Top, bottom
        for (int y = 0; y <= chunk.getHighestPoint(); y++) {
            short[][] topBlocks = new short[storage.getWidth()][storage.getDepth()];
            short[][] topMeta = new short[storage.getWidth()][storage.getDepth()];
            float[][] topLightLevels = new float[storage.getWidth()][storage.getDepth()];
            PerCornerLightData[][] topPcld = null;
            if(perCornerLight) {
                topPcld = new PerCornerLightData[storage.getWidth()][storage.getDepth()];
            }
            short[][] btmBlocks = new short[storage.getWidth()][storage.getDepth()];
            short[][] btmMeta = new short[storage.getWidth()][storage.getDepth()];
            float[][] btmLightLevels = new float[storage.getWidth()][storage.getDepth()];
            PerCornerLightData[][] btmPcld = null;
            if(perCornerLight) {
                btmPcld = new PerCornerLightData[storage.getWidth()][storage.getDepth()];
            }
            for (int x = 0; x < storage.getWidth(); x++) {
                for (int z = 0; z < storage.getDepth(); z++) {
                    try {
                        Block curBlock = storage.getBlock(x, y, z);
                        if (curBlock == null || !condition.shouldUse(curBlock))
                            continue;

                        if (y < storage.getHeight() - 1) {
                            if (!ocCond.shouldOcclude(curBlock, storage.getBlock(x, y + 1, z))) {
                                topBlocks[x][z] = (short)curBlock.getID();
                                topMeta[x][z] = storage.getMeta(x, y, z);
                                topLightLevels[x][z] = chunk.getLightLevel(x, y + 1, z);

                                if (perCornerLight) {
                                    PerCornerLightData pcld = new PerCornerLightData();
                                    pcld.l00 = calcPerCornerLight(Side.TOP, x, y, z);
                                    pcld.l01 = calcPerCornerLight(Side.TOP, x, y, z + 1);
                                    pcld.l10 = calcPerCornerLight(Side.TOP, x + 1, y, z);
                                    pcld.l11 = calcPerCornerLight(Side.TOP, x + 1, y, z + 1);
                                    topPcld[x][z] = pcld;
                                }
                            }
                        } else {
                            topBlocks[x][z] = (short) curBlock.getID();
                            if (perCornerLight) {
                                topPcld[x][z] = bright;
                            }
                            topLightLevels[x][z] = 1;
                        }
                        if (y > 0) {
                            if (!ocCond.shouldOcclude(curBlock, storage.getBlock(x, y - 1, z))) {
                                btmBlocks[x][z] = (short) curBlock.getID();
                                btmMeta[x][z] = storage.getMeta(x, y, z);
                                btmLightLevels[x][z] = chunk.getLightLevel(x, y - 1, z);

                                if (perCornerLight) {
                                    PerCornerLightData pcld = new PerCornerLightData();
                                    pcld.l00 = calcPerCornerLight(Side.BOTTOM, x, y, z);
                                    pcld.l01 = calcPerCornerLight(Side.BOTTOM, x, y, z + 1);
                                    pcld.l10 = calcPerCornerLight(Side.BOTTOM, x + 1, y, z);
                                    pcld.l11 = calcPerCornerLight(Side.BOTTOM, x + 1, y, z + 1);
                                    btmPcld[x][z] = pcld;
                                }
                            }
                        } else {
                            btmLightLevels[x][z] = 1;
                            continue;
                        }
                    } catch (CoordinatesOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            greedy(faces, Side.TOP, shouldMerge, topBlocks, topMeta, topLightLevels, topPcld, y + chunk.getStartPosition().y, chunk.getStartPosition().x, chunk.getStartPosition().z);
            greedy(faces, Side.BOTTOM, shouldMerge, btmBlocks, btmMeta, btmLightLevels, btmPcld, y + chunk.getStartPosition().y, chunk.getStartPosition().x, chunk.getStartPosition().z);
        }

        // East, west
        for (int x = 0; x < storage.getWidth(); x++) {
            short[][] westBlocks = new short[storage.getDepth()][chunk.getHighestPoint()+1];
            short[][] westMeta = new short[storage.getDepth()][chunk.getHighestPoint()+1];
            float[][] westLightLevels = new float[storage.getDepth()][chunk.getHighestPoint()+1];
            PerCornerLightData[][] westPcld = null;
            if(perCornerLight) {
                westPcld = new PerCornerLightData[storage.getDepth()][chunk.getHighestPoint()+1];
            }
            short[][] eastBlocks = new short[storage.getDepth()][chunk.getHighestPoint()+1];
            short[][] eastMeta = new short[storage.getDepth()][chunk.getHighestPoint()+1];
            float[][] eastLightLevels = new float[storage.getDepth()][chunk.getHighestPoint()+1];
            PerCornerLightData[][] eastPcld = null;
            if(perCornerLight) {
                eastPcld = new PerCornerLightData[storage.getDepth()][chunk.getHighestPoint()+1];
            }
            for (int z = 0; z < storage.getDepth(); z++) {
                for (int y = 0; y <= chunk.getHighestPoint(); y++) {
                    try {
                        Block curBlock = storage.getBlock(x, y, z);
                        if (curBlock == null || !condition.shouldUse(curBlock))
                            continue;

                        int westNeighborX = x - 1;
                        IChunk westNeighborChunk = chunk;
                        if(westNeighborX < 0) {
                            westNeighborChunk = chunk.getWorld().getChunk(chunk.getStartPosition().x + westNeighborX, chunk.getStartPosition().z + z);
                            westNeighborX += chunk.getWorld().getChunkSize();
                        }
                        if (westNeighborChunk != null) {
                            Block westNeighborBlk = westNeighborChunk.getBlock(westNeighborX, y, z);
                            if (!ocCond.shouldOcclude(curBlock, westNeighborBlk)) {
                                westBlocks[z][y] = (short)curBlock.getID();
                                westMeta[z][y] = storage.getMeta(x, y, z);
                                westLightLevels[z][y] = westNeighborChunk.getLightLevel(westNeighborX, y, z);

                                if (perCornerLight) {
                                    PerCornerLightData pcld = new PerCornerLightData();
                                    pcld.l00 = calcPerCornerLight(Side.WEST, x, y, z);
                                    pcld.l01 = calcPerCornerLight(Side.WEST, x, y, z + 1);
                                    pcld.l10 = calcPerCornerLight(Side.WEST, x, y + 1, z);
                                    pcld.l11 = calcPerCornerLight(Side.WEST, x, y + 1, z + 1);
                                    westPcld[z][y] = pcld;
                                }
                            }
                        } else {
                            westLightLevels[z][y] = 1;
                            continue;
                        }

                        int eastNeighborX = x + 1;
                        IChunk eastNeighborChunk = chunk;
                        if(eastNeighborX >= chunk.getWorld().getChunkSize()) {
                            eastNeighborChunk = chunk.getWorld().getChunk(chunk.getStartPosition().x + eastNeighborX, chunk.getStartPosition().z + z);
                            eastNeighborX -= chunk.getWorld().getChunkSize();
                        }
                        if (eastNeighborChunk != null) {
                            Block eastNeighborBlk = eastNeighborChunk.getBlock(eastNeighborX, y, z);
                            if (!ocCond.shouldOcclude(curBlock, eastNeighborBlk)) {
                                eastBlocks[z][y] = (short)curBlock.getID();
                                eastMeta[z][y] = storage.getMeta(x, y, z);
                                eastLightLevels[z][y] = eastNeighborChunk.getLightLevel(eastNeighborX, y, z);

                                if (perCornerLight) {
                                    PerCornerLightData pcld = new PerCornerLightData();
                                    pcld.l00 = calcPerCornerLight(Side.EAST, x, y, z);
                                    pcld.l01 = calcPerCornerLight(Side.EAST, x, y, z + 1);
                                    pcld.l10 = calcPerCornerLight(Side.EAST, x, y + 1, z);
                                    pcld.l11 = calcPerCornerLight(Side.EAST, x, y + 1, z + 1);
                                    eastPcld[z][y] = pcld;
                                }
                            }
                        } else {
                            eastLightLevels[z][y] = 1;
                            continue;
                        }
                    } catch (CoordinatesOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            greedy(faces, Side.EAST, shouldMerge, eastBlocks, eastMeta, eastLightLevels, eastPcld, x + chunk.getStartPosition().x, chunk.getStartPosition().z, chunk.getStartPosition().y);
            greedy(faces, Side.WEST, shouldMerge, westBlocks, westMeta, westLightLevels, westPcld, x + chunk.getStartPosition().x, chunk.getStartPosition().z, chunk.getStartPosition().y);
        }

        // North, south
        for (int z = 0; z < storage.getDepth(); z++) {
            short[][] northBlocks = new short[storage.getWidth()][chunk.getHighestPoint()+1];
            short[][] northMeta = new short[storage.getWidth()][chunk.getHighestPoint()+1];
            float[][] northLightLevels = new float[storage.getWidth()][chunk.getHighestPoint()+1];
            PerCornerLightData[][] northPcld = null;
            if(perCornerLight) {
                northPcld = new PerCornerLightData[storage.getWidth()][chunk.getHighestPoint()+1];
            }
            short[][] southBlocks = new short[storage.getWidth()][chunk.getHighestPoint()+1];
            short[][] southMeta = new short[storage.getWidth()][chunk.getHighestPoint()+1];
            float[][] southLightLevels = new float[storage.getWidth()][chunk.getHighestPoint()+1];
            PerCornerLightData[][] southPcld = null;
            if(perCornerLight) {
                southPcld = new PerCornerLightData[storage.getWidth()][chunk.getHighestPoint()+1];
            }
            for (int x = 0; x < storage.getWidth(); x++) {
                for (int y = 0; y <= chunk.getHighestPoint(); y++) {
                    try {
                        Block curBlock = storage.getBlock(x, y, z);
                        if (curBlock == null || !condition.shouldUse(curBlock))
                            continue;

                        int northNeighborZ = z + 1;
                        int southNeighborZ = z - 1;
                        IChunk northNeighborChunk = chunk;
                        IChunk southNeighborChunk = chunk;
                        if(northNeighborZ >= chunk.getWorld().getChunkSize()) {
                            northNeighborChunk = chunk.getWorld().getChunk(chunk.getStartPosition().x + x, chunk.getStartPosition().z + northNeighborZ);
                            northNeighborZ -= chunk.getWorld().getChunkSize();
                        } else if(southNeighborZ < 0) {
                            southNeighborChunk = chunk.getWorld().getChunk(chunk.getStartPosition().x + x, chunk.getStartPosition().z + southNeighborZ);
                            southNeighborZ += chunk.getWorld().getChunkSize();
                        }

                        if (northNeighborChunk != null) {
                            Block northNeighborBlock = northNeighborChunk.getBlock(x, y, northNeighborZ);
                            if (!ocCond.shouldOcclude(curBlock, northNeighborBlock)) {
                                northBlocks[x][y] = (short)curBlock.getID();
                                northMeta[x][y] = storage.getMeta(x, y, z);
                                northLightLevels[x][y] = northNeighborChunk.getLightLevel(x, y, northNeighborZ);

                                if (perCornerLight) {
                                    PerCornerLightData pcld = new PerCornerLightData();
                                    pcld.l00 = calcPerCornerLight(Side.NORTH, x, y, z);
                                    pcld.l01 = calcPerCornerLight(Side.NORTH, x, y + 1, z);
                                    pcld.l10 = calcPerCornerLight(Side.NORTH, x + 1, y, z);
                                    pcld.l11 = calcPerCornerLight(Side.NORTH, x + 1, y + 1, z);
                                    northPcld[x][y] = pcld;
                                }
                            }
                        } else {
                            northLightLevels[x][y] = 1;
                            continue;
                        }

                        if (southNeighborChunk != null) {
                            Block southNeighborBlock = southNeighborChunk.getBlock(x, y, southNeighborZ);
                            if (!ocCond.shouldOcclude(curBlock, southNeighborBlock)) {
                                southBlocks[x][y] = (short)curBlock.getID();
                                southMeta[x][y] = storage.getMeta(x, y, z);
                                southLightLevels[x][y] = southNeighborChunk.getLightLevel(x, y, southNeighborZ);

                                if (perCornerLight) {
                                    PerCornerLightData pcld = new PerCornerLightData();
                                    pcld.l00 = calcPerCornerLight(Side.SOUTH, x, y, z);
                                    pcld.l01 = calcPerCornerLight(Side.SOUTH, x, y + 1, z);
                                    pcld.l10 = calcPerCornerLight(Side.SOUTH, x + 1, y, z);
                                    pcld.l11 = calcPerCornerLight(Side.SOUTH, x + 1, y + 1, z);
                                    southPcld[x][y] = pcld;
                                }
                            }
                        } else {
                            southLightLevels[x][y] = 1;
                            continue;
                        }
                    } catch (CoordinatesOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            greedy(faces, Side.NORTH, shouldMerge, northBlocks, northMeta, northLightLevels, northPcld, z + chunk.getStartPosition().z, chunk.getStartPosition().x, chunk.getStartPosition().y);
            greedy(faces, Side.SOUTH, shouldMerge, southBlocks, southMeta, southLightLevels, southPcld, z + chunk.getStartPosition().z, chunk.getStartPosition().x, chunk.getStartPosition().y);
        }

        return faces;
    }

    public List<Face> getFaces(BlockStorage storage, UseCondition condition) {
        return getFaces(storage, condition,
                (curBlock, blockToSide) ->
                        !(blockToSide == null || (blockToSide.isTranslucent() && !curBlock.isTranslucent()))
                                && (curBlock.occludeCovered() && blockToSide.occludeCovered()),
                (id1, meta1, light1, pcld1, id2, meta2, light2, pcld2) -> {
                    Block block1 = VoxelGameAPI.instance.getBlockByID(id1);
                    if (!block1.shouldGreedyMerge())
                        return false;
                    boolean sameBlock = id1 == id2 && meta1 == meta2;
                    boolean sameLight = light1 == light2;
                    boolean tooDarkToTell = light1 < 0.1f; // Too dark to tell they're not the same block
                    if(perCornerLight) {
                        sameLight = pcld1.equals(pcld2);
                    }
                    if (sameLight && !sameBlock && tooDarkToTell) {
                        Block block2 = VoxelGameAPI.instance.getBlockByID(id2);
                        // Other block renderers may alter shape in an unpredictable way
                        if (block1.getRenderer().getClass() == NormalBlockRenderer.class
                                && block2.getRenderer().getClass() == NormalBlockRenderer.class
                                && !block1.isTranslucent() && !block2.isTranslucent())
                            sameBlock = true; // Consider them the same block
                    }
                    return sameBlock && sameLight;
                });
    }

    /**
     * @param outputList List to put faces in
     * @param side       Side being meshed
     * @param blks       Blocks on the plane
     * @param lls        Light levels of the blocks
     * @param z          Depth on the plane
     */
    private void greedy(List<Face> outputList, Side side, MergeCondition mergeCond, short[][] blks, short metadata[][], float lls[][], PerCornerLightData[][] pclds, int z, int offsetX, int offsetY) {
        int width = blks.length;
        int height = blks[0].length;
        boolean[][] used = new boolean[blks.length][blks[0].length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int blk = blks[x][y];
                if(blk == 0 || used[x][y])
                    continue;
                used[x][y] = true;
                float ll = lls[x][y];
                short meta = metadata[x][y];
                PerCornerLightData pcld = null;
                if(perCornerLight)
                    pcld = pclds[x][y];
                int endX = x + 1;
                int endY = y + 1;
                while (true) {
                    int newX = endX;
                    boolean shouldPass = false;
                    if (newX < width) {
                        int newBlk = blks[newX][y];
                        float newll = lls[newX][y];
                        short newMeta = metadata[newX][y];
                        PerCornerLightData newPcld = null;
                        if(perCornerLight)
                            newPcld = pclds[newX][y];
                        shouldPass = !used[newX][y] && newBlk != 0 && mergeCond.shouldMerge(blk, meta, ll, pcld, newBlk, newMeta, newll, newPcld);
                    }
                    // expand right if the same block
                    if (shouldPass) {
                        endX++;
                        used[newX][y] = true;
                    } else { // done on initial pass right. Start passing up.
                        while (true) {
                            if (endY == height) break;
                            boolean allPassed = true;
                            // sweep right
                            for (int lx = x; lx < endX; lx++) {
                                int lblk = blks[lx][endY];
                                if(lblk == 0) {
                                    allPassed = false;
                                    break;
                                }
                                short lmeta = metadata[lx][endY];
                                float llight = lls[lx][endY];
                                PerCornerLightData lPcld = null;
                                if(perCornerLight)
                                    lPcld = pclds[lx][endY];

                                if (used[lx][endY] || !mergeCond.shouldMerge(blk, meta, ll, pcld, lblk, lmeta, llight, lPcld)) {
                                    allPassed = false;
                                    break;
                                }
                            }
                            if (allPassed) {
                                for (int lx = x; lx < endX; lx++) {
                                    used[lx][endY] = true;
                                }
                                endY++;
                            } else {
                                break;
                            }
                        }
                        break;
                    }
                }
                outputList.add(new Face(side, blk, ll, pcld, x + offsetX, y + offsetY, endX + offsetX, endY + offsetY, z));
            }
        }
    }

    public Mesh meshFaces(List<Face> faces, MeshBuilder builder) {
        if(perCornerLight) {
            builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorUnpacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        } else {
            builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, GL20.GL_TRIANGLES);
        }
        builder.ensureVertices(faces.size()*4);
        for (Face f : faces) {
            f.render(builder);
        }
        return builder.end();
    }

    /**
     * Averages light values at a corner.
     *
     * @param side Side of the face being calculated
     * @param cx Chunk-relative X coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param y Chunk-relative Y coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param cz Chunk-relative Z coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     */
    private float calcPerCornerLight(Side side, int cx, int y, int cz) {
        // coordinate offsets for getting the blocks to average
        int posX = 0, negX = 0,
                posY = 0, negY = 0,
                posZ = 0, negZ = 0;
        switch(side) {
            case TOP:
                // Use the light values from the blocks above the face
                negY = posY = 1;
                // Get blocks around the point
                negZ = negX = -1;
                break;
            case BOTTOM:
                // Use the light values from the blocks below the face
                negY = posY = -1;
                // Get blocks around the point
                negZ = negX = -1;
                break;
            case WEST:
                // Use the light values from the blocks to the west of the face
                negX = posX = -1;
                // Get blocks around the point
                negY = negZ = -1;
                break;
            case EAST:
                // Use the light values from the blocks to the east of the face
                negX = posX = 1;
                // Get blocks around the point
                negY = negZ = -1;
                break;
            case NORTH:
                // Use the light values from the blocks to the north of the face
                negZ = posZ = 1;
                // Get blocks around the point
                negY = negX = -1;
                break;
            case SOUTH:
                // Use the light values from the blocks to the south of the face
                negZ = posZ = -1;
                // Get blocks around the point
                negY = negX = -1;
                break;
        }
        // sx,sy,sz are the x, y, and z positions of the side block
        int count = 0;
        float lightSum = 0;
        for(int sx = cx + negX; sx <= cx + posX; sx++) {
            for(int sy = y + negY; sy <= y + posY; sy++) {
                if(sy < 0 || sy >= chunk.getWorld().getHeight())
                    continue;
                for(int sz = cz + negZ; sz <= cz + posZ; sz++) {
                    IChunk sChunk = chunk;
                    boolean getChunk = false; // whether the block is not in the current chunk and a new chunk should be found
                    int getChunkX = chunk.getStartPosition().x + sx;
                    int getChunkZ = chunk.getStartPosition().z + sz;
                    int fixedSz = sz;
                    int fixedSx = sx;
                    if(sz < 0) {
                        fixedSz = chunk.getWorld().getChunkSize() + sz;
                        getChunk = true;
                    } else if(sz >= chunk.getWorld().getChunkSize()) {
                        fixedSz = sz - chunk.getWorld().getChunkSize();
                        getChunk = true;
                    }
                    if(sx < 0) {
                        fixedSx = chunk.getWorld().getChunkSize() + sx;
                        getChunk = true;
                    } else if(sx >= chunk.getWorld().getChunkSize()) {
                        fixedSx = sx - chunk.getWorld().getChunkSize();
                        getChunk = true;
                    }
                    if(getChunk) {
                        sChunk = chunk.getWorld().getChunk(getChunkX, getChunkZ);
                    }
                    if (sChunk == null)
                        continue;

                    try {
                        // Convert to chunk-relative coords
                        lightSum += sChunk.getLightLevel(fixedSx, sy, fixedSz);
                        count++;
                    } catch (CoordinatesOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return lightSum / count;
    }

    public static class Face {
        private final Side side;
        private final int x1, y1, x2, y2, z;
        private final Block block;
        private final float lightLevel;
        private final PerCornerLightData pcld;

        /**
         * @param pcld Per corner light data. Pass null if per corner lighting is disabled.
         */
        public Face(Side side, int block, float lightLevel, PerCornerLightData pcld, int startX, int startY, int endX, int endY, int z) {
            this.block = VoxelGameAPI.instance.getBlockByID(block);
            this.lightLevel = lightLevel;
            this.x1 = startX;
            this.y1 = startY;
            this.x2 = endX;
            this.y2 = endY;
            this.z = z;
            this.side = side;
            this.pcld = pcld;
        }

        public void render(MeshBuilder builder) {
            IBlockRenderer renderer = block.getRenderer();

            switch (side) {
                case TOP:
                    renderer.renderTop(block.getTextureIndex(), x1, y1, x2, y2, z + 1, lightLevel, pcld, builder);
                    break;
                case BOTTOM:
                    renderer.renderBottom(block.getTextureIndex(), x1, y1, x2, y2, z, lightLevel, pcld, builder);
                    break;
                case NORTH:
                    renderer.renderNorth(block.getTextureIndex(), x1, y1, x2, y2, z + 1, lightLevel, pcld, builder);
                    break;
                case SOUTH:
                    renderer.renderSouth(block.getTextureIndex(), x1, y1, x2, y2, z, lightLevel, pcld, builder);
                    break;
                case EAST:
                    renderer.renderEast(block.getTextureIndex(), x1, y1, x2, y2, z + 1, lightLevel, pcld, builder);
                    break;
                case WEST:
                    renderer.renderWest(block.getTextureIndex(), x1, y1, x2, y2, z, lightLevel, pcld, builder);
                    break;

            }
        }
    }

    public interface OccludeCondition {
        /**
         * @param curBlock Current block being checked
         * @param blockToSide Block the the side of the current block
         * @return True if the side of the curBlock should be occluded
         */
        boolean shouldOcclude(Block curBlock, Block blockToSide);
    }

    public interface MergeCondition {
        boolean shouldMerge(int id1, int meta1, float light1, PerCornerLightData pcld1, int id2, int meta2, float light2, PerCornerLightData pcld2);
    }

}
