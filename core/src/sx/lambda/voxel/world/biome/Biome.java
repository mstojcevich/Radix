package sx.lambda.voxel.world.biome;

import com.badlogic.gdx.math.MathUtils;

public class Biome {
    // Biome color math from https://github.com/erich666/Mineways/blob/master/Win/biomes.cpp

    private static final int[][] grassColors =
    {
        { 191, 183,  85 },	// lower left, temperature starts at 1.0 on left
        { 128, 180, 151 },	// lower right
        {  71, 205,  51 }	// upper left
    };

    private static final int[][] foliageCorners =
    {
        { 174, 164,  42 },	// lower left, temperature starts at 1.0 on left
        {  96, 161, 123 },	// lower right
        {  26, 191,  0 }	// upper left
    };

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

    public int[] getGrassColor(int elevation) {
        return getColor(elevation, grassColors);
    }

    public int[] getFoliageColor(int elevation) {
        return getColor(elevation, foliageCorners);
    }

    private int[] getColor(int elevation, int[][] corners) {
        float adjTemp = MathUtils.clamp(temperature - elevation*0.00166667f, 0, 1);
        float adjRainfall = MathUtils.clamp(rainfall, 0, 1) * adjTemp;

        float lambda[] = new float[3];
        lambda[0] = adjTemp - adjRainfall;
        lambda[1] = 1 - temperature;
        lambda[2] = rainfall;

        float red = 0, green = 0, blue = 0;
        for(int i = 0; i < 3; i++) {
            red += lambda[i] * corners[i][0/*red*/];
            green += lambda[i] * corners[i][1/*green*/];
            blue += lambda[i] * corners[i][2/*blue*/];
        }

        return new int[]{
                (int)MathUtils.clamp(red, 0, 255),
                (int)MathUtils.clamp(green, 0, 255),
                (int)MathUtils.clamp(blue, 0, 255)
        };
    }

}
