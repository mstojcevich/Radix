package sx.lambda.mstojcevich.voxel.render

import org.lwjgl.BufferUtils

import java.nio.FloatBuffer

import static org.lwjgl.opengl.GL15.*
import static org.lwjgl.opengl.GL11.*

/**
 * Renders things on the screen using VBOs
 */
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

    /**
     * Initializes the renderer
     *
     * MAKE SURE OGL IS SETUP BEFORE RUNNING
     */
    public void init() {
        vboColorHandle = glGenBuffers()
        vboVertexHandle = glGenBuffers()

        vertexPositionData = BufferUtils.createFloatBuffer(vertices*dimensions)
        vertexColorData = BufferUtils.createFloatBuffer(vertices*3/*R G B*/)

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
            glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle)
            glBufferData(GL_ARRAY_BUFFER, vertexPositionData, GL_STATIC_DRAW)
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle)
            glBufferData(GL_ARRAY_BUFFER, vertexColorData, GL_STATIC_DRAW)
            glBindBuffer(GL_ARRAY_BUFFER, 0)
        } finally {
            cooked = true
        }
    }

    public void drawVboQuads() throws NotInitializedException, InvalidBakeStateException {
        drawVbo(GL_QUADS)
    }

    public void drawVbo(int drawMode) throws NotInitializedException, InvalidBakeStateException {
        if(!initialized)throw new NotInitializedException()
        if(!cooked)throw new InvalidBakeStateException(cooked)

        glPushMatrix()
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle)
        glVertexPointer(dimensions, GL_FLOAT, 0, 0l)
        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle)
        glColorPointer(dimensions, GL_FLOAT, 0, 0l)
        glDrawArrays(drawMode, 0, vertices)
        glPopMatrix()
    }

}
