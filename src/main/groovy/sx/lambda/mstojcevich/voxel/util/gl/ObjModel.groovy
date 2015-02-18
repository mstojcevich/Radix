package sx.lambda.mstojcevich.voxel.util.gl

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.util.gl.OBJLoader.Model

import static org.lwjgl.opengl.GL11.*

@CompileStatic
class ObjModel {

    private final Model m
    private int displayList

    ObjModel(InputStream is) {
        m = OBJLoader.loadModel(is)
    }

    void render() {
        if(displayList != -1) {
            glCallList displayList
        }
    }

    void init() {
        displayList = OBJLoader.createDisplayList(m)
    }

}
