package sx.lambda.mstojcevich.voxel.shader;

import org.lwjgl.LWJGLException;
import sx.lambda.mstojcevich.voxel.util.gl.ShaderProgram;

public class PostProcessShader extends ShaderProgram {

    private int vCoordLoc = -1;
    private int fboTexCoordLoc = -1;
    private int animTimeLoc = -1;

    public PostProcessShader(String vertex, String fragment) throws LWJGLException {
        super(vertex, fragment);
        vCoordLoc = getAttributeLocation("position");
        fboTexCoordLoc = getAttributeLocation("texCoord");
        animTimeLoc = getUniformLocation("animTime");
    }

    public void setAnimTime(int ms) {
        setUniformf(animTimeLoc, ms);
    }

}
