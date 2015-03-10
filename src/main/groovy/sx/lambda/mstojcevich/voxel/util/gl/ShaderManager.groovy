package sx.lambda.mstojcevich.voxel.util.gl

import groovy.transform.CompileStatic
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

@CompileStatic
class ShaderManager {

    private int currentShaderID = -1

    private int enableLightingLoc = -1
    private int enableTexturingLoc = -1

    private ShaderProgram currentProgram

    public void setShader(ShaderProgram program) {
        int id = program.getID()
        if(currentShaderID != id) {
            currentShaderID = id
            GL20.glUseProgram(program.getID())
            currentProgram = program
            this.enableLightingLoc = currentProgram.getUniformLocation("enableLighting")
            this.enableTexturingLoc = currentProgram.getUniformLocation("enableTexturing")
        }
    }

    public void enableLighting() {
        if(currentShaderID != -1) {
            currentProgram.setUniformi(enableLightingLoc, 1)
        } else {
            GL11.glEnable(GL11.GL_LIGHTING)
        }
    }

    public void disableLighting() {
        if(currentShaderID != -1) {
            currentProgram.setUniformi(enableLightingLoc, 0)
        } else {
            GL11.glDisable(GL11.GL_LIGHTING)
        }
    }

    public void enableTexturing() {
        if(currentShaderID != -1) {
            currentProgram.setUniformi(enableTexturingLoc, 1)
        } else {
            GL11.glEnable(GL11.GL_TEXTURE_2D)
        }
    }

    public void disableTexturing() {
        if(currentShaderID != -1) {
            currentProgram.setUniformi(enableTexturingLoc, 0)
        } else {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, -1)
        }
    }

}
