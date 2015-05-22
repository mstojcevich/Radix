/*
package sx.lambda.voxel.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.BufferUtils
import groovy.transform.CompileStatic

import java.nio.FloatBuffer

*/
/**
 * Renders things on the screen using VBOs
 *//*

@CompileStatic
class Tessellator {

    private int vboColorHandle = -1
    private int vboVertexHandle = -1

    private boolean initialized

    private final int vertices, dimensions

    private FloatBuffer vertexPositionData, vertexColorData

    private boolean cooked

    public Tessellator(int vertices, int dimensions) {
        this.vertices = vertices
        this.dimensions = dimensions
    }

    */
/**
 * Initializes the renderer
 *
 * MAKE SURE OGL IS SETUP BEFORE RUNNING
 *//*

    public void init() {
        vboColorHandle = Gdx.gl.glGenBuffer()
        vboVertexHandle = Gdx.gl.glGenBuffer()

        vertexPositionData = BufferUtils.newFloatBuffer(vertices * dimensions)
        vertexColorData = BufferUtils.newFloatBuffer(vertices*3*/
/*R G B*//*
)

        initialized = true
    }

    public void putVertex(float x1, float y1, float z1,
                          float x2, float y2, float z2,
                          float x3, float y3, float z3,
                          float x4, float y4, float z4) throws NotInitializedException, InvalidBakeStateException {
        if(!initialized)throw new NotInitializedException()
        if(cooked)throw new InvalidBakeStateException(cooked)

        float[] newPoint = [x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4]
        vertexPositionData.put(newPoint)
    }

    public void putColor(r1, g1, b1, r2, g2, b2, r3, g3, b3, r4, g4, b4) {
        float[] newPoint = [r1, g1, b1, r2, g2, b2, r3, g3, b3, r4, g4, b4];
        vertexColorData.put(newPoint)
    }

    public void cook() throws InvalidBakeStateException, NotInitializedException {
        if(!initialized) throw new NotInitializedException()
        if(cooked) throw new InvalidBakeStateException(cooked)
        try {
            vertexPositionData.flip()
            vertexColorData.flip()
            Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, vboVertexHandle)
            Gdx.gl.glBufferData(Gdx.gl.GL_ARRAY_BUFFER, vertexPositionData.remaining() << 2, vertexPositionData, Gdx.gl.GL_STATIC_DRAW)
            Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, 0)
            Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, vboColorHandle)
            Gdx.gl.glBufferData(Gdx.gl.GL_ARRAY_BUFFER, vertexColorData.remaining() << 2, vertexColorData, Gdx.gl.GL_STATIC_DRAW)
            Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, 0)
        } finally {
            cooked = true
        }
    }

    public void drawVboQuads() throws NotInitializedException, InvalidBakeStateException {
        drawVbo(Gdx.gl.GL_QUADS)
    }

    public void drawVbo(int drawMode) throws NotInitializedException, InvalidBakeStateException {
        if(!initialized)throw new NotInitializedException()
        if(!cooked)throw new InvalidBakeStateException(cooked)

        Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, vboVertexHandle)
        Gdx.gl.glVertexPointer(dimensions, Gdx.gl.GL_FLOAT, 0, 0l)
        Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, vboColorHandle)
        Gdx.gl.glColorPointer(dimensions, Gdx.gl.GL_FLOAT, 0, 0l)
        Gdx.gl.glDrawArrays(drawMode, 0, vertices)
    }

}
*/
