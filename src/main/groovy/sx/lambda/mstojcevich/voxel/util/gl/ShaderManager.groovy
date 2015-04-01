package sx.lambda.mstojcevich.voxel.util.gl

import groovy.transform.CompileStatic
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

@CompileStatic
class ShaderManager {

    private int currentShaderID = -1

    private int enableLightingLoc = -1
    private int enableTexturingLoc = -1
    private int screenSizeLoc = -1
    private int cameraRangeLoc = -1
    private int animTimeLoc = -1
    private int enableWaveLoc = -1
    private int chunkOffsetLoc = -1
    private int blockIdAttr = -1

    private ShaderProgram currentProgram

    public void setShader(ShaderProgram program) {
        int id = program.getID()
        if(currentShaderID != id) {
            currentShaderID = id
            GL20.glUseProgram(program.getID())
            currentProgram = program

            this.enableLightingLoc = currentProgram.getUniformLocation("enableLighting")
            this.enableTexturingLoc = currentProgram.getUniformLocation("enableTexturing")
            this.screenSizeLoc = currentProgram.getUniformLocation("screensize")
            this.cameraRangeLoc = currentProgram.getUniformLocation("camerarange")
            this.animTimeLoc = currentProgram.getUniformLocation("animTime")
            this.enableWaveLoc = currentProgram.getUniformLocation("enableWave")
            this.chunkOffsetLoc = currentProgram.getUniformLocation("chunkOffset")
            this.blockIdAttr = currentProgram.getAttributeLocation("blockIdAttr")

            updateScreenSize()
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

    public void updateScreenSize() {
        if(currentShaderID != -1) {
            currentProgram.setUniformi(screenSizeLoc, Display.width, Display.height)
        }
    }

    public void onPerspective(float near, float far) {
        if(currentShaderID != -1) {
            currentProgram.setUniformf(cameraRangeLoc, near, far)
        }
    }

    public void setAnimTime(int ms) {
        if(currentShaderID != -1) {
            currentProgram.setUniformi(animTimeLoc, ms)
        }
    }

    public void enableWave() {
        if (currentShaderID != -1) {
            currentProgram.setUniformi(enableWaveLoc, 1)
        }
    }

    public void disableWave() {
        if (currentShaderID != -1) {
            currentProgram.setUniformi(enableWaveLoc, 0)
        }
    }

    public void setChunkOffset(float x, float y, float z) {
        if (currentShaderID != -1) {
            currentProgram.setUniformf(chunkOffsetLoc, x, y, z);
        }
    }

}
