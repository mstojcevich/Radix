package sx.lambda.voxel.texture

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import groovy.transform.CompileStatic

@CompileStatic
class TextureManager {

    private int boundTexture;

    public void bindTexture(int id) {
        if(id == -1)this.boundTexture = id; //-1 = reset. Don't send gl command but still reset bound texture.
        if(this.boundTexture != id) {
            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, id)
            this.boundTexture = id;
        }
    }

}
