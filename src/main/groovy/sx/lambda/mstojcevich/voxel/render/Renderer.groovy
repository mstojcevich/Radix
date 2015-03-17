package sx.lambda.mstojcevich.voxel.render

import groovy.transform.CompileStatic

@CompileStatic
public interface Renderer {

    void render()

    void init()

    void draw2d()

    void cleanup()

}