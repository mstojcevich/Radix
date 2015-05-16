package sx.lambda.voxel.shader;

import sx.lambda.voxel.util.gl.ShaderProgram;

public class WorldShader extends ShaderProgram {

    private int blkIdAtr, posAtr, clrAtr, nrmAtr;

    private boolean lightingEnabled, texturingEnabled, wavesEnabled;
    private final int lightToggleUni, texToggleUni, waveToggleUni;
    private final int animTimeUni;

    public WorldShader(String vertex, String fragment) {
        super(vertex, fragment);

        blkIdAtr = getAttributeLocation("blockIdAttr");
        posAtr = getAttributeLocation("positionAttr");
        clrAtr = getAttributeLocation("lightingAndAlphaAttr");
        nrmAtr = getAttributeLocation("normalAttr");

        lightToggleUni = getUniformLocation("enableLighting");
        texToggleUni = getUniformLocation("enableTexturing");
        waveToggleUni = getUniformLocation("enableWave");

        animTimeUni = getUniformLocation("animTime");
    }

    public int getBlockIdAttr() { return blkIdAtr; }
    public int getPositionAttr() { return posAtr; }
    public int getLightingAlphaAttr() { return clrAtr; }
    public int getNormalAttr() { return nrmAtr; }

    public void enableLighting() {
        if(!this.lightingEnabled) {
            setUniformi(lightToggleUni, 1);
            this.lightingEnabled = true;
        }
    }

    public void disableLighting() {
        if(this.lightingEnabled) {
            setUniformi(lightToggleUni, 0);
            this.lightingEnabled = false;
        }
    }

    public void enableTexturing() {
        if(!this.texturingEnabled) {
            setUniformi(texToggleUni, 1);
            this.texturingEnabled = true;
        }
    }

    public void disableTexturing() {
        if(this.texturingEnabled) {
            setUniformi(texToggleUni, 0);
            this.texturingEnabled = false;
        }
    }

    public void enableWaves() {
        if(!this.wavesEnabled) {
            setUniformi(waveToggleUni, 1);
            this.wavesEnabled = true;
        }
    }

    public void disableWaves() {
        if(this.wavesEnabled) {
            setUniformi(waveToggleUni, 0);
            this.wavesEnabled = false;
        }
    }

    public void updateAnimTime() {
        setUniformi(animTimeUni, (int)(System.currentTimeMillis() % 100000));
    }

}
