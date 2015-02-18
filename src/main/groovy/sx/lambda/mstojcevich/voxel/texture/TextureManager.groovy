package sx.lambda.mstojcevich.voxel.texture

import groovy.transform.CompileStatic
import org.lwjgl.opengl.GL11

@CompileStatic
class TextureManager {

    private int boundTexture;

    public void bindTexture(int id) {
        if(id == -1)this.boundTexture = id; //-1 = reset. Don't send gl command but still reset bound texture.
        if(this.boundTexture != id) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
            this.boundTexture = id;
        }
    }

}
