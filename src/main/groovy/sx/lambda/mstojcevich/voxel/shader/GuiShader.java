package sx.lambda.mstojcevich.voxel.shader;

import org.lwjgl.LWJGLException;
import sx.lambda.mstojcevich.voxel.util.gl.ShaderProgram;

/**
 * Wrapper for the shader used for plain 2D draws
 */
public class GuiShader extends ShaderProgram {

    private int positionAttrib = -1,
                texCoordAttrib = -1,
                colorAttrib = -1,
                enableTexturingUniform = -1,
                enableColorsUniform = -1;

    private boolean texturingEnabled, colorsEnabled;

    /**
     * @param vertex Source code for the vertex shader
     * @param fragment Source code for the fragment shader
     */
    public GuiShader(String vertex, String fragment) throws LWJGLException {
        super(vertex, fragment);

        positionAttrib = getAttributeLocation("position");
        texCoordAttrib = getAttributeLocation("texCoord");
        enableColorsUniform = getAttributeLocation("enableColors");
        enableTexturingUniform = getAttributeLocation("enableTexturing");

    }

    public int getPositionAttrib() {
        return positionAttrib;
    }

    public int getTexCoordAttrib() {
        return texCoordAttrib;
    }

    public int getColorAttrib() {
        return colorAttrib;
    }

    public void enableTexturing() {
        if(!texturingEnabled) {
            texturingEnabled = true;
            setUniformi(enableTexturingUniform, 1);
        }
    }

    public void disableTexturing() {
        if(texturingEnabled) {
            texturingEnabled = false;
            setUniformi(enableTexturingUniform, 0);
        }
    }

    public void enableColors() {
        if(!colorsEnabled) {
            colorsEnabled = true;
            setUniformi(enableColorsUniform, 1);
        }
    }

    public void disableColors() {
        if(colorsEnabled) {
            colorsEnabled = false;
            setUniformi(enableColorsUniform, 0);
        }
    }

}
