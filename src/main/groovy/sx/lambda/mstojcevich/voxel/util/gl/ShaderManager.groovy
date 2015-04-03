package sx.lambda.mstojcevich.voxel.util.gl

import groovy.transform.CompileStatic
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

@CompileStatic
class ShaderManager {

    private int currentShaderID = -1

    private ShaderProgram currentProgram

    public void setShader(ShaderProgram program) {
        int id = program.getID()
        if(currentShaderID != id) {
            currentShaderID = id
            GL20.glUseProgram(program.getID())
            currentProgram = program
        }
    }

}
