package sx.lambda.mstojcevich.voxel.entity

import groovy.transform.CompileStatic
import static org.lwjgl.opengl.GL11.*
import sx.lambda.mstojcevich.voxel.util.gl.ObjModel

@CompileStatic
abstract class Entity implements Serializable {

    private final EntityPosition position;
    private final EntityRotation rotation;
    private transient ObjModel model;
    private int id = -1

    public Entity(ObjModel model, EntityPosition position, EntityRotation rotation) {
        this.model = model
        this.position = position
        this.rotation = rotation
    }

    public Entity(ObjModel model) {
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
        if(model != null) {
            glPushMatrix()
            glTranslatef(position.x, position.y, position.z)
            model.render()
            glPopMatrix()
        }
    }

    /**
     * Initialization once an opengl context is available
     */
    public void init() {
        if(model != null) {
            model.init()
        }
    }

    public void setModel(ObjModel model) {
        this.model = model
    }

    public ObjModel getDefaultModel() {
        return null
    }

    public void setID(int id) {
        this.id = id
    }

    public int getID() {
        id
    }

}
