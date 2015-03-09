package sx.lambda.mstojcevich.voxel

import static org.lwjgl.opengl.GL11.*

/**
 * Created by marcusant on 10/10/2014.
 */
class CubeRenderer {

    private int x, y, z, size

    public CubeRenderer(int x, int y, int z, int size) {
        this.x = x
        this.y = y
        this.z = z
        this.size = size
    }

    public void draw() {
        int u = 0
        int v = 0
        int u2 = 0
        int v2 = 0

        //top
        glBegin GL_QUADS
            glTexCoord2f(u, v); glVertex3f(x + 1, y, z+1)
            glTexCoord2f(u, v2); glVertex3f(x, y, z+1)
            glTexCoord2f(u2, v2); glVertex3f(x, y + 1, z+1)
            glTexCoord2f(u2, v); glVertex3f(x + 1, y + 1, z+1)
        glEnd()

        //left
        glBegin GL_QUADS
            glTexCoord2f(u, v); glVertex3f(x, y, z)
            glTexCoord2f(u, v2); glVertex3f(x, y, z + 1)
            glTexCoord2f(u2, v2); glVertex3f(x, y + 1, z + 1)
            glTexCoord2f(u2, v); glVertex3f(x, y + 1, z)
        glEnd()

        //right
        glBegin GL_QUADS
            glTexCoord2f(u, v); glVertex3f(x + 1, y, z)
            glTexCoord2f(u, v2); glVertex3f(x + 1, y + 1, z)
            glTexCoord2f(u2, v2); glVertex3f(x + 1, y + 1, z + 1)
            glTexCoord2f(u2, v); glVertex3f(x + 1, y, z + 1)
        glEnd()

        //front
        glBegin GL_QUADS
            glTexCoord2f(u, v); glVertex3f(x, y, z)
            glTexCoord2f(u, v2); glVertex3f(x + 1, y, z)
            glTexCoord2f(u2, v2); glVertex3f(x + 1, y, z + 1)
            glTexCoord2f(u2, v); glVertex3f(x, y, z + 1)
        glEnd()

        //back
        glBegin GL_QUADS
            glTexCoord2f(u, v); glVertex3f(x + 1, y + 1, z)
            glTexCoord2f(u, v2); glVertex3f(x, y + 1, z)
            glTexCoord2f(u2, v2); glVertex3f(x, y + 1, z + 1)
            glTexCoord2f(u2, v); glVertex3f(x + 1, y + 1, z + 1)
        glEnd()

        //bottom
        glBegin GL_QUADS
            glTexCoord2f(u, v); glVertex3f(x + 1, y, z)
            glTexCoord2f(u, v2); glVertex3f(x, y, z)
            glTexCoord2f(u2, v2); glVertex3f(x, y + 1, z)
            glTexCoord2f(u2, v); glVertex3f(x + 1, y + 1, z)
        glEnd()
    }

}
