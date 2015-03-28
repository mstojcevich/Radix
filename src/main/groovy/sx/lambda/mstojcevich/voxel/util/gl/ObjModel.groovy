package sx.lambda.mstojcevich.voxel.util.gl

import groovy.transform.CompileStatic
import org.lwjgl.opengl.GL15
import sx.lambda.mstojcevich.voxel.util.gl.OBJLoader.Model

import static org.lwjgl.opengl.GL11.*

@CompileStatic
class ObjModel {

    private final Model m
    private int posVbo, normalVbo

    ObjModel(InputStream is) {
        m = OBJLoader.loadModel(is)
    }

    void render() {
        if(posVbo > 0) {
            if(m.hasNormals()) {
                glEnableClientState(GL_NORMAL_ARRAY)
            } else {
                glDisableClientState(GL_NORMAL_ARRAY)
            }
            glDisableClientState(GL_COLOR_ARRAY)
            glDisableClientState(GL_TEXTURE_COORD_ARRAY)
            glEnableClientState(GL_VERTEX_ARRAY)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posVbo)
            glVertexPointer(3, GL_FLOAT, 0, 0)
            if(m.hasNormals()) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo)
                glNormalPointer(GL_FLOAT, 0, 0)
            }

            glDrawArrays(GL_TRIANGLES, 0, m.getFaces().size()*3);

            if(!m.hasNormals()) {
                glEnableClientState(GL_NORMAL_ARRAY)
            }
            glEnableClientState(GL_TEXTURE_COORD_ARRAY)
            glEnableClientState(GL_COLOR_ARRAY)
        }
    }

    void init() {
        int[] vbos = OBJLoader.createVBO(m)
        posVbo = vbos[0]
        normalVbo = vbos[1]
    }

}
