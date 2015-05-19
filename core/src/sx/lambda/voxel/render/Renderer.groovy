package sx.lambda.voxel.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import groovy.transform.CompileStatic

@CompileStatic
public interface Renderer {

    void render()

    void init()

    void draw2d(SpriteBatch guiBatch)

    void cleanup()

}