package sx.lambda.voxel.api;

import pw.oxcafebabe.marcusant.eventbus.EventManager;
import pw.oxcafebabe.marcusant.eventbus.managers.iridium.IridiumEventManager;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.block.*;
import sx.lambda.voxel.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class RadixAPI {

    public static final RadixAPI instance = new RadixAPI();
    private final EventManager eventManager;
    private final List<Block> registeredBlocks = new ArrayList<>();
    private final Block[] registeredBlockArray = new Block[1024];
    private final Biome[] registeredBiomeArray = new Biome[256];
    private int highestID = 0;

    private RadixAPI() {
        this.eventManager = new IridiumEventManager();
    }

    /**
     * Gets the event manager that is used to fire and listen to API events
     */
    public EventManager getEventManager() {
        return this.eventManager;
    }

    /**
     * @return Whether the current game is a client
     */
    public boolean isClient() {
        return RadixClient.getInstance() != null;
    }

    /**
     * Registers the built in blocks
     * <p/>
     * If you're a mod, please don't call this
     */
    public void registerBuiltinBlocks() throws BlockRegistrationException {
        try {
            IBlockRenderer foliageRenderer = new FlatFoliageRenderer();
            IBlockRenderer coloredFoliageRenderer = new ColoredFoliageRenderer();
            registerBlocks(
                    new BlockBuilder().setID(BuiltInBlockIds.DIRT_ID).setHumanName("Dirt").setTextureLocation("textures/block/dirt.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.GRASS_ID).setHumanName("Grass").setTextureLocations("textures/block/grass_top.png", "textures/block/grass_side.png", "textures/block/dirt.png").setBlockRenderer(new GrassRenderer()).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.STONE_ID).setHumanName("Stone").setTextureLocation("textures/block/stone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.COBBLESTONE_ID).setHumanName("Cobblestone").setTextureLocation("textures/block/cobblestone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.MOSS_STONE_ID).setHumanName("Mossy Cobblestone").setTextureLocation("textures/block/moss_stone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.SAND_ID).setHumanName("Sand").setTextureLocation("textures/block/sand.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.CLAY_ID).setHumanName("Clay").setTextureLocation("textures/block/clay.png").build(),
                    new Liquid(BuiltInBlockIds.WATER_ID, "Water (Still)", "textures/block/water.png", 0),
                    new Liquid(BuiltInBlockIds.WATER_FLOW_ID, "Water (Flowing)", "textures/block/water.png", 0),
                    new Liquid(BuiltInBlockIds.LAVA_STILL_ID, "Lava (Still)", "textures/block/lava.png", 15),
                    new Liquid(BuiltInBlockIds.LAVA_FLOW_ID, "Lava (Flowing)", "textures/block/lava.png", 15),
                    new BlockBuilder().setID(BuiltInBlockIds.PLANKS_ID).setHumanName("Planks").setTextureLocation("textures/block/planks.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.STONE_BRICK_ID).setHumanName("Stone Brick").setTextureLocation("textures/block/stonebrick.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.TALL_GRASS_ID).setHumanName("Tall Grass").setTextureLocation("textures/block/tallgrass.png").setSolid(false).setTranslucent(true).setOccludeCovered(false).setLightPassthrough(true).setBlockRenderer(new TallGrassRenderer()).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.FLOWER_ID).setHumanName("Flower").setTextureLocation("textures/block/flower_tulip_orange.png").setSolid(false).setTranslucent(true).setOccludeCovered(false).setLightPassthrough(true).setBlockRenderer(foliageRenderer).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.FLOWER_TWO_ID).setHumanName("Flower").setTextureLocation("textures/block/flower_rose.png").setSolid(false).setTranslucent(true).setOccludeCovered(false).setLightPassthrough(true).setBlockRenderer(foliageRenderer).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.FLOWER_THREE_ID).setHumanName("Flower").setTextureLocation("textures/block/shrub.png").setSolid(false).setTranslucent(true).setOccludeCovered(false).setLightPassthrough(true).setBlockRenderer(foliageRenderer).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.SUGAR_CANE_ID).setHumanName("Sugar Cane").setTextureLocation("textures/block/sugarcane.png").setSolid(false).setTranslucent(true).setLightPassthrough(true).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LOG_ID).setHumanName("Log").setTextureLocation("textures/block/log_oak.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LOG_TWO_ID).setHumanName("Log").setTextureLocation("textures/block/log_oak.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LEAVES_ID).setHumanName("Leaves").setTextureLocation("textures/block/leaves_oak.png").setBlockRenderer(coloredFoliageRenderer).setTranslucent(true).setOccludeCovered(false).setDecreaseLight(false).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.LEAVES_TWO_ID).setHumanName("Leaves").setTextureLocation("textures/block/leaves_oak.png").setBlockRenderer(coloredFoliageRenderer).setTranslucent(true).setOccludeCovered(false).setDecreaseLight(false).build(),
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
                    new Snow(),
                    new BlockBuilder().setID(BuiltInBlockIds.STONE_MONSTER_EGG_ID).setHumanName("Stone (Monster Egg)").setTextureLocation("textures/block/stone.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.RED_MUSHROOM_BLOCK_ID).setHumanName("Red Mushroom Block").setTextureLocation("textures/block/mushroom_block_skin_red.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.BROWN_MUSHROOM_BLOCK_ID).setHumanName("Brown Mushroom Block").setTextureLocation("textures/block/mushroom_block_skin_brown.png").build(),
                    new BlockBuilder().setID(BuiltInBlockIds.BROWN_MUSHROOM_ID).setHumanName("Brown Mushroom").setTextureLocation("textures/block/mushroom_brown.png").setTranslucent(true).setLightPassthrough(true).setSolid(false).build(),
                    new BlockBuilder().setID(BuiltInBlockIds.RED_MUSHROOM_ID).setHumanName("Red Mushroom").setTextureLocation("textures/block/mushroom_red.png").setTranslucent(true).setLightPassthrough(true).setSolid(false).build(),
                    new Fence(),
                    new BlockBuilder().setID(BuiltInBlockIds.UNKNOWN_ID).setHumanName("Unknown").setTextureLocation("textures/block/undefined.png").build()
            );
        } catch (BlockBuilder.MissingElementException ex) {
            throw new BlockRegistrationException(ex.getMessage());
        }
    }

    public void registerMinecraftBiomes() {
        registerBiomes(
                /*
                Temperature and rainfall values taken from http://minecraft.gamepedia.com/Biome
                 */
                new Biome(0, "Ocean", 0.5f, 0.5f),
                new Biome(1, "Plains", 0.8f, 0.4f),
                new Biome(2, "Desert", 2.0f, 0.0f),
                new Biome(3, "Extreme Hills", 0.2f, 0.3f),
                new Biome(4, "Forest", 0.7f, 0.8f),
                new Biome(5, "Taiga", 0.05f, 0.8f),
                new Biome(6, "Swampland", 0.8f, 0.9f),
                new Biome(7, "River", 0.5f, 0.5f),
                new Biome(8, "Nether", 2.0f, 0.0f),
                new Biome(9, "End", 0.5f, 0.5f),
                new Biome(10, "Frozen Ocean", 0.0f, 0.5f),
                new Biome(11, "Frozen River", 0.0f, 0.5f),
                new Biome(12, "Ice Plains", 0.0f, 0.5f),
                new Biome(13, "Ice Mountains", 0.0f, 0.5f),
                new Biome(14, "Mushroom Island", 0.9f, 1.0f),
                new Biome(15, "Mushroom Island Shore", 0.9f, 1.0f),
                new Biome(16, "Beach", 0.8f, 0.4f),
                new Biome(17, "Desert Hills", 0.8f, 0.4f),
                new Biome(18, "Forest Hills", 0.7f, 0.8f),
                new Biome(19, "Taiga Hills", 0.2f, 0.7f),
                new Biome(20, "Extreme Hills Edge", 0.2f, 0.3f),
                new Biome(21, "Jungle", 1.2f, 0.9f),
                new Biome(22, "Jungle Hills", 1.2f, 0.9f),
                new Biome(23, "Jungle Edge", 0.95f, 0.8f),
                new Biome(24, "Deep Ocean", 0.5f, 0.5f),
                new Biome(25, "Stone Beach", 0.2f, 0.3f),
                new Biome(26, "Cold Beach", 0.05f, 0.3f),
                new Biome(27, "Birch Forest", 0.6f, 0.6f),
                new Biome(28, "Birch Forest Hills", 0.6f, 0.6f),
                new Biome(29, "Roofed Forest", 0.7f, 0.8f),
                new Biome(30, "Cold Taiga", -0.5f, 0.4f),
                new Biome(31, "Cold Taiga Hills", -0.5f, 0.4f),
                new Biome(32, "Mega Taiga", 0.3f, 0.8f),
                new Biome(33, "Mega Taiga Hills", 0.3f, 0.8f),
                new Biome(34, "Extreme Hills+", 0.2f, 0.3f),
                new Biome(35, "Savanna", 1.2f, 0.0f),
                new Biome(36, "Savanna Plateau", 1.0f, 0.0f),
                new Biome(37, "Mesa", 2.0f, 0.0f),
                new Biome(38, "Mesa Plateau F", 2.0f, 0.0f),
                new Biome(39, "Mesa Plateau", 2.0f, 0.0f)

        );
    }

    private void registerBiomes(Biome... biomes) {
        for(Biome b : biomes) {
            registerBiome(b);
        }
    }

    private void registerBiome(Biome biome) {
        registeredBiomeArray[biome.getID()] = biome;
    }

    /**
     * Gets the biome with the specific ID
     *
     * If using Minecraft IDs, try the id-128 as variations are usually just parent+128
     */
    public Biome getBiomeByID(int id) {
        if(id >= registeredBiomeArray.length || id < 0)
            return null;
        return registeredBiomeArray[id];
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
