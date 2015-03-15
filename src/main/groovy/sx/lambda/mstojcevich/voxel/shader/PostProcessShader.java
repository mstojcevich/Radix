package sx.lambda.mstojcevich.voxel.shader;

import org.lwjgl.LWJGLException;
import sx.lambda.mstojcevich.voxel.util.gl.ShaderProgram;

public class PostProcessShader extends ShaderProgram {

    private int vCoordLoc = -1;
    private int fboTexCoordLoc = -1;
    private int fboColorLoc = -1;

    public PostProcessShader(String vertex, String fragment) throws LWJGLException {
        super(vertex, fragment);
        vCoordLoc = getAttributeLocation("position");
        fboColorLoc = getAttributeLocation("color");
        fboTexCoordLoc = getAttributeLocation("texCoord");
        System.out.println(vCoordLoc);
        System.out.println(fboColorLoc);
        System.out.println(fboTexCoordLoc);
    }

}
