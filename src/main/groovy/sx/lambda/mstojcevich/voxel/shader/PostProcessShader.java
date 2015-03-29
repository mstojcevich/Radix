package sx.lambda.mstojcevich.voxel.shader;

import org.lwjgl.LWJGLException;
import sx.lambda.mstojcevich.voxel.VoxelGame;
import sx.lambda.mstojcevich.voxel.util.gl.ShaderProgram;

public class PostProcessShader extends ShaderProgram {

    private int vCoordLoc = -1;
    private int fboTexCoordLoc = -1;
    private int animTimeLoc = -1;

    public PostProcessShader(String vertex, String fragment) throws LWJGLException {
        super(vertex, fragment);
        VoxelGame.getInstance().getShaderManager().setShader(this);
        vCoordLoc = getAttributeLocation("position");
        fboTexCoordLoc = getAttributeLocation("texCoord");
        animTimeLoc = getUniformLocation("animTime");

        if(VoxelGame.getInstance().getSettingsManager().getVisualSettings().isPeasantModeEnabled()) {
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
