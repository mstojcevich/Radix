package sx.lambda.voxel.world.biome;

public class Biome {

    private final String humanName;
    private final float temperature, rainfall;
    private final int id;

    public Biome(int id, String humanName, float temperature, float rainfall) {
        this.humanName = humanName;
        this.temperature = temperature;
        this.rainfall = rainfall;
        this.id = id;
    }

    public float getTemperature() {
        return this.temperature;
    }

    public float getRainfall() {
        return this.rainfall;
    }

    public int getID() {
        return id;
    }

}
