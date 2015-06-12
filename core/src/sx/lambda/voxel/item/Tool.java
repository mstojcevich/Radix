package sx.lambda.voxel.item;

public class Tool extends Item {

    private final ToolMaterial material;
    private final ToolType type;

    public Tool(int id, String humanName, ToolMaterial material, ToolType type) {
        super(id, humanName);

        this.material = material;
        this.type = type;
    }

    public ToolMaterial getMaterial() {
        return this.material;
    }

    public ToolType getType() {
        return this.type;
    }

    public enum ToolType {
        SHOVEL, PICKAXE, AXE, SHEARS, THESE_HANDS
    }

    public enum ToolMaterial {
        THESE_HANDS(0, 1), WOOD(1, 2), STONE(2, 4), IRON(3, 6), GOLD(3, 12), DIAMOND(4, 8);

        /**
         * Multiplier on break speed of breakable blocks
         */
        public final float speedMult;
        /**
         * Strength value to determine whether a block can be broken
         */
        public final int materialStrength;

        ToolMaterial(int materialStrength, float speedMult) {
            this.materialStrength = materialStrength;
            this.speedMult = speedMult;
        }
    }

}
