package sx.lambda.voxel.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import org.spacehq.mc.protocol.data.game.values.entity.MobType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by louxiu on 12/22/15.
 */
public class EntityModel {
    private static Map<MobType, ModelInstance> modelInstances = new HashMap<>();
    private static Map<MobType, Model> models = new HashMap<>();
    private static Map<MobType, Texture> textures = new HashMap<>();

    private static ModelInstance playerModelInstance;
    private static Model playerModel;
    private static Texture playerTexture;
    private static ObjLoader loader = new ObjLoader();
    // TODO: make it configurable
    private static final String ENTITY_PATH = "textures/world/entity";

    public static void init() {
        playerModelInstance = getInstance(ENTITY_PATH + "/human/human.obj", ENTITY_PATH + "/human/human.png");
        modelInstances.put(MobType.CREEPER, getInstance(ENTITY_PATH + "/creeper/creeper.obj", ENTITY_PATH + "/creeper/creeper.png"));
        modelInstances.put(MobType.ENDERMAN, getInstance(ENTITY_PATH + "/enderman/enderman.obj", ENTITY_PATH + "/enderman/enderman.png"));
        modelInstances.put(MobType.SKELETON, getInstance(ENTITY_PATH + "/skeleton/skeleton.obj", ENTITY_PATH + "/skeleton/skeleton.png"));
        modelInstances.put(MobType.ZOMBIE, getInstance(ENTITY_PATH + "/zombie/zombie.obj", ENTITY_PATH + "/zombie/zombie.png"));
        modelInstances.put(MobType.ZOMBIE_PIGMAN, getInstance(ENTITY_PATH + "/pigzombie/pigzombie.obj", ENTITY_PATH + "/pigzombie/pigzombie.png"));

        playerModel = getModel(ENTITY_PATH + "/human/human.obj");
        models.put(MobType.CREEPER, getModel(ENTITY_PATH + "/creeper/creeper.obj"));
        models.put(MobType.ENDERMAN, getModel(ENTITY_PATH + "/enderman/enderman.obj"));
        models.put(MobType.SKELETON, getModel(ENTITY_PATH + "/skeleton/skeleton.obj"));
        models.put(MobType.ZOMBIE, getModel(ENTITY_PATH + "/zombie/zombie.obj"));
        models.put(MobType.ZOMBIE_PIGMAN, getModel(ENTITY_PATH + "/pigzombie/pigzombie.obj"));

        playerTexture = getTexture(ENTITY_PATH + "/human/human.png");
        textures.put(MobType.CREEPER, getTexture(ENTITY_PATH + "/creeper/creeper.png"));
        textures.put(MobType.ENDERMAN, getTexture(ENTITY_PATH + "/enderman/enderman.png"));
        textures.put(MobType.SKELETON, getTexture(ENTITY_PATH + "/skeleton/skeleton.png"));
        textures.put(MobType.ZOMBIE, getTexture(ENTITY_PATH + "/zombie/zombie.png"));
        textures.put(MobType.ZOMBIE_PIGMAN, getTexture(ENTITY_PATH + "/pigzombie/pigzombie.png"));
    }

    public static ModelInstance getInstance(MobType type) {
        return modelInstances.get(type);
    }

    private static ModelInstance getInstance(String objPath, String texturePath) {
        Model model = loader.loadModel(Gdx.files.internal(objPath), new ObjLoader.ObjLoaderParameters(true));
        ModelInstance instance = new ModelInstance(model);
        Texture texture = new Texture(Gdx.files.internal(texturePath));
        instance.materials.get(0).set(TextureAttribute.createDiffuse(texture));
        return instance;
    }

    public static Model getModel(MobType type) {
        return models.get(type);
    }

    private static Model getModel(String objPath) {
        return loader.loadModel(Gdx.files.internal(objPath), new ObjLoader.ObjLoaderParameters(true));
    }

    public static Texture getTexture(MobType type) {
        return textures.get(type);
    }

    private static Texture getTexture(String texturePath) {
        return new Texture(Gdx.files.internal(texturePath));
    }

    // player
    public static ModelInstance getPlayerInstance() {
        return playerModelInstance;
    }

    public static Model getPlayerModel() {
        return playerModel;
    }

    public static Texture getPlayerTexture() {
        return playerTexture;
    }
}
