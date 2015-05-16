package sx.lambda.voxel.util.gl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient

import static com.badlogic.gdx.graphics.GL20.*

@CompileStatic
class ObjModel {

    private final Model m

    private ModelBatch batch;

    ObjModel(FileHandle handle) {
        //m = new ObjLoader().loadModel(handle)
//        batch = new ModelBatch();
    }

    void render() {
        // TODO implement
    }

    void init() {
    }

}
