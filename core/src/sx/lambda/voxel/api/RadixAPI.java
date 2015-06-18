package sx.lambda.voxel.api;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import pw.oxcafebabe.marcusant.eventbus.EventManager;
import pw.oxcafebabe.marcusant.eventbus.managers.iridium.IridiumEventManager;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.block.*;
import sx.lambda.voxel.item.Item;
import sx.lambda.voxel.item.Tool;
import sx.lambda.voxel.item.Tool.ToolMaterial;
import sx.lambda.voxel.item.Tool.ToolType;
import sx.lambda.voxel.world.biome.Biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadixAPI {

    public static final RadixAPI instance = new RadixAPI();
    private final EventManager eventManager;
    private final List<Block> registeredBlocks = new ArrayList<>();
    private final List<Item> registeredItems = new ArrayList<>();
    private final Map<String, BlockRenderer> registeredRenderers = new HashMap<>();
    private final Block[] registeredBlockArray = new Block[1024];
    private final Item[] registeredItemArray = new Item[1024];
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
     * Registers the built in items and blocks
     *
     * If you're a mod, please don't call this
     *
     * Called before the item registration event is fired.
     */
    public void registerBuiltinItems() throws BlockRegistrationException {
        try {
            registerItems(
                    new Tool(270, "Wooden Pickaxe", ToolMaterial.WOOD, ToolType.PICKAXE),
                    new Tool(274, "Stone Pickaxe", ToolMaterial.STONE, ToolType.PICKAXE),
                    new Tool(257, "Iron Pickaxe", ToolMaterial.IRON, ToolType.PICKAXE)
            );

            String builtinBlockJson = Gdx.files.internal("defaultRegistry/blocks.json").readString();
            try {
                JsonBlock[] blocks = loadBlocks(new Gson(), builtinBlockJson);
                for(JsonBlock block : blocks) {
                    registerBlock(block.createBlock());
                }
            } catch (BlockBuilder.CustomClassException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (BlockBuilder.MissingElementException ex) {
            throw new BlockRegistrationException(ex.getMessage());
        }
    }

    /**
     * Register builtin block renderers.
     *
     * If you're a mod, please don't call this.
     *
     * Called before the BlockRenderer registration event is fired.
     *
     * @throws DuplicateRendererException Usually thrown if this is called twice or someone tried to register a block renderer that's already built in.
     */
    public void registerBuiltinBlockRenderers() throws DuplicateRendererException {
        registerBlockRenderers(
                new NormalBlockRenderer(),
                new ColoredFoliageRenderer(),
                new FlatFoliageRenderer(),
                new GrassRenderer(),
                new TallGrassRenderer(),
                new MetadataHeightRenderer(7), // used for snow
                new MetadataHeightRenderer(15, true), // used for liquids
                new FenceRenderer()

        );
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

    public void registerBiomes(Biome... biomes) {
        for(Biome b : biomes) {
            registerBiome(b);
        }
    }

    public void registerBiome(Biome biome) {
        registeredBiomeArray[biome.getID()] = biome;
    }

    public void registerBlockRenderers(BlockRenderer ... renderers) throws DuplicateRendererException {
        for(BlockRenderer br : renderers) {
            registerBlockRenderer(br);
        }
    }

    public void registerBlockRenderer(BlockRenderer renderer) throws DuplicateRendererException {
        String id = renderer.getUniqueID();
        if(registeredRenderers.containsKey(id)) {
            throw new DuplicateRendererException(id);
        }
        this.registeredRenderers.put(renderer.getUniqueID(), renderer);
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

    private void registerItems(Item... items) throws BlockRegistrationException {
        for (Item i : items) {
            registerItem(i);
        }
    }

    private void registerItem(Item i) throws BlockRegistrationException {
        if (i.getID() == -1) {
            i.setID(highestID);
            System.err.printf("Item ID not defined for %s. Using auto generated ID. IF YOU\'RE THE MOD DEVELOPER, FIX THIS!!!\n", i.toString());
        }

        for (Item i2 : this.registeredItems) {
            if (i.getID() == i2.getID()) {
                throw new BlockRegistrationException("ID already in use by " + i2.getHumanName());
            }
        }

        this.registeredItems.add(i);
        this.registeredItemArray[i.getID()] = i;
        highestID = Math.max(highestID, i.getID());
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

        registerItem(b);
    }

    public List<Block> getBlocks() {
        return registeredBlocks;
    }

    /**
     * Get block by its ID.
     * Should only be used with static IDs for builtin blocks since mods will often (and should) have configurable IDs.
     *
     * @param id ID that the block has in the registry.
     * @return null (should be treated as air) if id is 0 or below, is greater than the maximum allowed id, or is not registered.
     */
    public Block getBlock(int id) {
        if(id <= 0)
            return null;
        if(id >= registeredBlockArray.length)
            return null;

        return registeredBlockArray[id];
    }

    /**
     * Get item by its ID.
     * Should only be used with static IDs for builtin items since mods will often (and should) have configurable IDs.
     *
     * @param id ID that the item has in the registry.
     * @return null if id is 0 or below, is greater than the maximum allowed id, or is not registered.
     */
    public Item getItem(int id) {
        if(id <= 0)
            return null;
        if(id >= registeredItemArray.length)
            return null;

        return registeredItemArray[id];
    }

    /**
     * Get the BlockRenderer registered with the specified unique ID
     *
     * @param uid Unique ID of the renderer to get. Returned by BlockRenderer.getUniqueID().
     * @throws sx.lambda.voxel.api.RadixAPI.NoSuchRendererException No renderer existed with the specified UID.
     */
    public BlockRenderer getBlockRenderer(String uid) throws NoSuchRendererException {
        BlockRenderer renderer = registeredRenderers.get(uid);
        if(renderer != null) {
            return renderer;
        } else {
            throw new NoSuchRendererException(uid);
        }
    }

    public Block[] getBlocksSorted() {
        return registeredBlockArray;
    }

    public class BlockRegistrationException extends Exception {
        public BlockRegistrationException(String reason) {
            super(reason);
        }
    }

    public class NoSuchRendererException extends BlockRegistrationException {
        public NoSuchRendererException(String renderer) {
            super(String.format(
                    "Renderer \"%s\" does not exist. " +
                            "Make sure you registered it before the block load phase. Also check for typos.",
                    renderer
            ));
        }

        public NoSuchRendererException(String blockName, String renderer) {
            super(String.format(
                    "Renderer \"%s\" does not exist for block \"%s\". " +
                            "Make sure you registered it before the block load phase. Also check for typos.",
                    renderer, blockName
            ));
        }
    }

    public class DuplicateRendererException extends BlockRegistrationException {
        public DuplicateRendererException(String renderer) {
            super(String.format(
                    "Renderer with id \"%s\" already exists and is cannot be redifined. " +
                            "Make sure you are using a unique ID and are not registering twice.",
                    renderer
            ));
        }
    }

    private JsonBlock[] loadBlocks(Gson gson, String json) {
        return gson.fromJson(json, JsonBlock[].class);
    }

}
