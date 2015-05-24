package sx.lambda.voxel.api;

import pw.oxcafebabe.marcusant.eventbus.EventManager;
import pw.oxcafebabe.marcusant.eventbus.managers.iridium.IridiumEventManager;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.BlockBuilder;
import sx.lambda.voxel.block.WaterRenderer;
import sx.lambda.voxel.server.VoxelGameServer;

import java.util.ArrayList;
import java.util.List;

public class VoxelGameAPI {

    public static final VoxelGameAPI instance = new VoxelGameAPI();
    private final EventManager eventManager;
    private List<Block> registeredBlocks = new ArrayList<>();
    private Block[] registeredBlockArray = new Block[Short.MAX_VALUE];
    private int highestID = 0;

    public VoxelGameAPI() {
        this.eventManager = new IridiumEventManager();
    }

    /**
     * Gets the event manager that is used to fire and listen to API events
     */
    public EventManager getEventManager() {
        return this.eventManager;
    }

    /**
     * @return Whether the current game is a server
     */
    public boolean isServer() {
        return VoxelGameServer.instance != null;
    }

    /**
     * @return Whether the current game is a client\
     */
    public boolean isClient() {
        return VoxelGameClient.getInstance() != null;
    }

    /**
     * Registers the built in blocks
     * <p/>
     * If you're a mod, please don't call this
     */
    public void registerBuiltinBlocks() throws BlockRegistrationException {
        try {
            registerBlocks(
                    new BlockBuilder().setID(BuiltInBlockIds.DIRT_ID).setHumanName("Dirt").setTextureLocation("textures/block/dirt.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.GRASS_ID).setHumanName("Grass").setTextureLocation("textures/block/grass.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.STONE_ID).setHumanName("Stone").setTextureLocation("textures/block/stone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.SAND_ID).setHumanName("Sand").setTextureLocation("textures/block/sand.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.WATER_ID).setHumanName("Water").setSolid(false).setTransparent(true).setTextureLocation("textures/block/water.png").setBlockRenderer(new WaterRenderer(BuiltInBlockIds.WATER_ID)).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.PLANKS_ID).setHumanName("Planks").setTextureLocation("textures/block/planks.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.STONE_BRICK_ID).setHumanName("Stone Brick").setTextureLocation("textures/block/stonebrick.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.UNKNOWN_ID).setHumanName("Unknown").setTextureLocation("textures/block/undefined.png").build()
            );
        } catch (BlockBuilder.MissingElementException ex) {
            throw new BlockRegistrationException(ex.getMessage());
        }
    }

    private void registerBlocks(Block... blocks) throws BlockRegistrationException {
        for (Block b : blocks) {
            registerBlock(b);
        }

    }

    private void registerBlock(Block b) throws BlockRegistrationException {
        if (b.getID() == -1) {
            b.setID(highestID);
            System.err.println("Block ID not defined for " + String.valueOf(b) + ". Using auto generated ID. IF YOU\'RE THE MOD DEVELOPER, FIX THIS!!!");
        }

        for (Block bl : this.registeredBlocks) {
            if (bl.getID() == b.getID()) {
                throw new BlockRegistrationException("ID already in use by " + b.getHumanName());
            }

        }

        this.registeredBlocks.add(b);
        this.registeredBlockArray[b.getID()] = b;
        highestID = Math.max(highestID, b.getID());
    }

    public List<Block> getBlocks() {
        return registeredBlocks;
    }

    public Block getBlockByID(int id) {
        if(id <= 0)return null;
        return registeredBlockArray[id];
    }

    public Block[] getBlocksSorted() {
        return registeredBlockArray;
    }

    public class BlockRegistrationException extends Exception {
        public BlockRegistrationException(String reason) {
            super(reason);
        }
    }

}
