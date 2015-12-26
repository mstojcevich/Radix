package sx.lambda.voxel.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

import java.io.Serializable;

public abstract class Entity implements Serializable {
    private final EntityPosition position;
    private final EntityRotation rotation;
    private Model model;
    private Texture texture;
    private int id = -1;

    public Entity(Model model, Texture texture, EntityPosition position, EntityRotation rotation) {
        this.model = model;
        this.texture = texture;
        this.position = position;
        this.rotation = rotation;
    }

    public Entity() {
        this.model = getDefaultModel();
        this.texture = getDefaultTexture();
        this.position = new EntityPosition(0, 0, 0);
        this.rotation = new EntityRotation(0, 0);
    }

    public EntityPosition getPosition() {
        return position;
    }

    public EntityRotation getRotation() {
        return rotation;
    }

    public void render(ModelBatch batch) {
        if (model != null) {
            // TODO: support subtle action. need to create instance every time?
            ModelInstance instance = new ModelInstance(model);
            instance.materials.get(0).set(TextureAttribute.createDiffuse(texture));
            instance.transform.translate(position.getX(), position.getY(), position.getZ());
            batch.render(instance);
        }
    }

    /**
     * Initialization once an opengl context is available
     */
    public void init() {
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Model getDefaultModel() {
        return null;
    }

    public Texture getDefaultTexture() {
        return null;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    /**
     * Called 20 times a second
     */
    public void onUpdate() {
    }
}
