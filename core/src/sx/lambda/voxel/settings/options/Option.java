package sx.lambda.voxel.settings.options;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface Option<E> {

    /**
     * Get the title of the option
     */
    String getTitle();

    /**
     * Creates and returns an actor that can be used to manipulate the option
     */
    Actor getManipulationActor();

    /**
     * Get a string representation of the value of the option
     */
    String getValueStr();

    E getValue();

    void setValue(E value);

}
