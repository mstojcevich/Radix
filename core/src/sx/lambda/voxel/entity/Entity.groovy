package sx.lambda.voxel.entity

import com.badlogic.gdx.graphics.g3d.Model
import groovy.transform.CompileStatic

@CompileStatic
abstract class Entity implements Serializable {

    private final EntityPosition position;
    private final EntityRotation rotation;
    private transient Model model;
    private int id = -1

    public Entity(Model model, EntityPosition position, EntityRotation rotation) {
        this.model = model
        this.position = position
        this.rotation = rotation
    }

    public Entity(Model model) {
        this(model, new EntityPosition(0, 0, 0), new EntityRotation(0, 0))
    }

    public Entity() {
        this(getDefaultModel())
    }

    public EntityPosition getPosition() {
        position
    }

    public EntityRotation getRotation() {
        rotation
    }

    public void render() {
        //TODO implement
        if (model != null) {
//            Vector3 oldPos = VoxelGameClient.instance.camera.position
//            VoxelGameClient.instance.camera.position = new Vector3(position.x, position.y, position.z)
//            VoxelGameClient.instance.camera.update()
//            model.render()
//            VoxelGameClient.instance.camera.translate(oldPos)
//            VoxelGameClient.instance.camera.update()
        }
    }

    /**
     * Initialization once an opengl context is available
     */
    public void init() {
    }

    public void setModel(Model model) {
        this.model = model
    }

    public Model getDefaultModel() {
        return null
    }

    public void setID(int id) {
        this.id = id
    }

    public int getID() {
        id
    }

    /**
     * Called 20 times a second
     */
    public void onUpdate() {}

}
