package sx.lambda.voxel.shader;

import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.util.gl.ShaderProgram;

public class PostProcessShader extends ShaderProgram {

    private int vCoordLoc = -1;
    private int fboTexCoordLoc = -1;
    private int animTimeLoc = -1;

    public PostProcessShader(String vertex, String fragment) {
        super(vertex, fragment);
        VoxelGameClient.getInstance().getShaderManager().setShader(this);
        vCoordLoc = getAttributeLocation("position");
        fboTexCoordLoc = getAttributeLocation("texCoord");
        animTimeLoc = getUniformLocation("animTime");

        if(VoxelGameClient.getInstance().getSettingsManager().getVisualSettings().isPeasantModeEnabled()) {
            setUniformi("nextGen", 1);
        }
    }

    public void setAnimTime(int ms) {
        setUniformf(animTimeLoc, ms);
    }

    public int getPositionAttrib() {
        return vCoordLoc;
    }

    public int getTexCoordAttrib() {
        return fboTexCoordLoc;
    }

}
