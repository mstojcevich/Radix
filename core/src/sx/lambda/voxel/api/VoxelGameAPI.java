package sx.lambda.voxel.api;

import pw.oxcafebabe.marcusant.eventbus.EventManager;
import pw.oxcafebabe.marcusant.eventbus.managers.iridium.IridiumEventManager;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.block.BlockBuilder;
import sx.lambda.voxel.block.FoliageBlockRenderer;
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
                    new BlockBuilder().setID(BuiltInBlockIds.COBBLESTONE_ID).setHumanName("Cobblestone").setTextureLocation("textures/block/cobblestone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.MOSS_STONE_ID).setHumanName("Mossy Cobblestone").setTextureLocation("textures/block/moss_stone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.SAND_ID).setHumanName("Sand").setTextureLocation("textures/block/sand.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.CLAY_ID).setHumanName("Clay").setTextureLocation("textures/block/clay.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.WATER_ID).setHumanName("Water").setSolid(false).setSelectable(false).setTranslucent(true).setLightPassthrough(true).setTextureLocation("textures/block/water.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LAVA_STILL_ID).setHumanName("Lava (Still)").setSolid(false).setSelectable(false).setTextureLocation("textures/block/lava.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LAVA_FLOW_ID).setHumanName("Lava (Flowing)").setSolid(false).setSelectable(false).setTextureLocation("textures/block/lava.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.PLANKS_ID).setHumanName("Planks").setTextureLocation("textures/block/planks.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.STONE_BRICK_ID).setHumanName("Stone Brick").setTextureLocation("textures/block/stonebrick.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.TALL_GRASS_ID).setHumanName("Tall Grass").setTextureLocation("textures/block/tallgrass.png").setSolid(false).setTranslucent(true).setLightPassthrough(true).setBlockRenderer(new FoliageBlockRenderer(BuiltInBlockIds.TALL_GRASS_ID)).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.FLOWER_ID).setHumanName("Flower").setTextureLocation("textures/block/flower_tulip_orange.png").setSolid(false).setTranslucent(true).setLightPassthrough(true).setBlockRenderer(new FoliageBlockRenderer(BuiltInBlockIds.FLOWER_ID)).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.FLOWER_TWO_ID).setHumanName("Flower").setTextureLocation("textures/block/flower_rose.png").setSolid(false).setTranslucent(true).setLightPassthrough(true).setBlockRenderer(new FoliageBlockRenderer(BuiltInBlockIds.FLOWER_TWO_ID)).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.FLOWER_THREE_ID).setHumanName("Flower").setTextureLocation("textures/block/shrub.png").setSolid(false).setTranslucent(true).setLightPassthrough(true).setBlockRenderer(new FoliageBlockRenderer(BuiltInBlockIds.FLOWER_THREE_ID)).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.SUGAR_CANE_ID).setHumanName("Sugar Cane").setTextureLocation("textures/block/sugarcane.png").setSolid(false).setTranslucent(true).setLightPassthrough(true).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LOG_ID).setHumanName("Log").setTextureLocation("textures/block/log_oak.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LOG_TWO_ID).setHumanName("Log").setTextureLocation("textures/block/log_oak.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LEAVES_ID).setHumanName("Leaves").setTextureLocation("textures/block/leaves_oak.png").setTranslucent(true).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LEAVES_TWO_ID).setHumanName("Leaves").setTextureLocation("textures/block/leaves_oak.png").setTranslucent(true).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.BEDROCK_ID).setHumanName("Bedrock").setTextureLocation("textures/block/bedrock.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.IRON_ORE_ID).setHumanName("Iron Ore").setTextureLocation("textures/block/iron_ore.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.COAL_ORE_ID).setHumanName("Coal Ore").setTextureLocation("textures/block/coal_ore.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.GOLD_ORE_ID).setHumanName("Gold Ore").setTextureLocation("textures/block/gold_ore.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.DIAMOND_ORE_ID).setHumanName("Diamond Ore").setTextureLocation("textures/block/diamond_ore.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.REDSTONE_ORE_ID).setHumanName("Redstone Ore").setTextureLocation("textures/block/redstone_ore.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LAPIS_ORE_ID).setHumanName("Lapis Ore").setTextureLocation("textures/block/lapis_ore.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.EMERALD_ORE_ID).setHumanName("Emerald Ore").setTextureLocation("textures/block/emerald_ore.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.OBSIDIAN_ID).setHumanName("Obsidian").setTextureLocation("textures/block/obsidian.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.GRAVEL_ID).setHumanName("Gravel").setTextureLocation("textures/block/gravel.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.SNOW_ID).setHumanName("Snow").setTextureLocation("textures/block/snow.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.STONE_MONSTER_EGG_ID).setHumanName("Stone (Monster Egg)").setTextureLocation("textures/block/stone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.RED_MUSHROOM_BLOCK_ID).setHumanName("Red Mushroom Block").setTextureLocation("textures/block/mushroom_block_skin_red.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.BROWN_MUSHROOM_BLOCK_ID).setHumanName("Brown Mushroom Block").setTextureLocation("textures/block/mushroom_block_skin_brown.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.BROWN_MUSHROOM_ID).setHumanName("Brown Mushroom").setTextureLocation("textures/block/mushroom_brown.png").setTranslucent(true).setLightPassthrough(true).setSolid(false).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.RED_MUSHROOM_ID).setHumanName("Red Mushroom").setTextureLocation("textures/block/mushroom_red.png").setTranslucent(true).setLightPassthrough(true).setSolid(false).build(),
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
