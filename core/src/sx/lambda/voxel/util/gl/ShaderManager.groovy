package sx.lambda.voxel.util.gl

import groovy.transform.CompileStatic

@CompileStatic
class ShaderManager {

    private ShaderProgram currentProgram

    public void setShader(ShaderProgram program) {
        currentProgram = program
        currentProgram.begin()
    }

}
