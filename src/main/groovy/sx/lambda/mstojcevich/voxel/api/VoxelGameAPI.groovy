package sx.lambda.mstojcevich.voxel.api

import groovy.transform.CompileStatic
import pw.oxcafebabe.marcusant.eventbus.EventManager
import pw.oxcafebabe.marcusant.eventbus.managers.iridium.IridiumEventManager
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.block.BlockBuilder
import sx.lambda.mstojcevich.voxel.block.WaterRenderer
import sx.lambda.mstojcevich.voxel.server.VoxelGameServer

@CompileStatic
class VoxelGameAPI {

    public static final VoxelGameAPI instance = new VoxelGameAPI()

    private final EventManager eventManager

    private List<Block> registeredBlocks = new ArrayList<>()
    Block[] registeredBlockArray = new Block[Short.MAX_VALUE]
    private int highestID = 0

    public VoxelGameAPI() {
        this.eventManager = new IridiumEventManager()
    }

    /**
     * Gets the event manager that is used to fire and listen to API events
     */
    public EventManager getEventManager() { return this.eventManager }

    /**
     * @return Whether the current game is a server
     */
    public boolean isServer() {
        return VoxelGameServer.instance != null
    }

    /**
     * @return Whether the current game is a client\
     */
    public boolean isClient() {
        return VoxelGame.instance != null
    }

    /**
     * Registers the built in blocks
     *
     * If you're a mod, please don't call this
     */
    public void registerBuiltinBlocks() throws BlockRegistrationException {
        registerBlocks(
            new BlockBuilder().setID(BuiltInBlockIds.DIRT_ID).setHumanName("Dirt").setTextureLocation(getClass().getResource("/textures/block/dirt.png")).build(),
                new BlockBuilder().setID(BuiltInBlockIds.GRASS_ID).setHumanName("Grass").setTextureLocation(getClass().getResource("/textures/block/grass.png")).build(),
                new BlockBuilder().setID(BuiltInBlockIds.STONE_ID).setHumanName("Stone").setTextureLocation(getClass().getResource("/textures/block/stone.png")).build(),
                new BlockBuilder().setID(BuiltInBlockIds.SAND_ID).setHumanName("Sand").setTextureLocation(getClass().getResource("/textures/block/sand.png")).build(),
                new BlockBuilder().setID(BuiltInBlockIds.WATER_ID).setHumanName("Water").setSolid(false).setTransparent(true).setTextureLocation(getClass().getResource("/textures/block/water.png")).setBlockRenderer(new WaterRenderer(BuiltInBlockIds.WATER_ID)).build()
        )
    }

    public void registerBlocks(Block ... blocks) throws BlockRegistrationException {
        for(Block b : blocks) {
            registerBlock(b)
        }
    }

    public void registerBlock(Block b) throws BlockRegistrationException {
        if(b.ID == -1) {
            b.ID = highestID
            System.err.println("Block ID not defined for $b. Using auto generated ID. IF YOU'RE THE MOD DEVELOPER, FIX THIS!!!")
        }
        for(Block bl : this.registeredBlocks) {
            if(bl.ID == b.ID) {
                throw new BlockRegistrationException("ID already in use by $b.humanName")
            }
        }

        this.registeredBlocks.add(b)
        this.registeredBlockArray[b.ID] = b
        highestID = Math.max(highestID, b.ID)
    }

    public class BlockRegistrationException extends Exception {
        public BlockRegistrationException(String reason) { super(reason) }
    }

    public List<Block> getBlocks() { return registeredBlocks }

    public Block getBlockByID(int id) {
        return registeredBlockArray[id]
    }

    public Block[] getBlocksSorted() {
        return registeredBlockArray;
    }

}
