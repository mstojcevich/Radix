package sx.lambda.mstojcevich.voxel.util.gl

import groovy.transform.CompileStatic
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import sx.lambda.mstojcevich.voxel.VoxelGame
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
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posVbo)
            GL20.glVertexAttribPointer(VoxelGame.instance.worldShader.positionAttr, 3, GL_FLOAT, false, 0, 0)
            if(m.hasNormals()) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVbo)
                GL20.glVertexAttribPointer(VoxelGame.instance.worldShader.normalAttr, 3, GL_FLOAT, false, 0, 0)
            }

            glDrawArrays(GL_TRIANGLES, 0, m.getFaces().size()*3);
        }
    }

    void init() {
        int[] vbos = OBJLoader.createVBO(m)
        posVbo = vbos[0]
        normalVbo = vbos[1]
    }

}
