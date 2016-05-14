package net.devintia.commons.bukkit.armorstand;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to store all loaded models to keep track of them and remove them on disable
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class ArmorStandModelHandler {

    private List<ArmorStandModel> models = new ArrayList<>();

    /**
     * Called onDisable, will despawn all remaining models
     */
    public void disable() {
        for ( ArmorStandModel model : models ) {
            model.despawn();
        }

        models.clear();
    }

    void add( ArmorStandModel model ) {
        models.add( model );
    }

    /**
     * @param name the name of the model
     * @return the reference to the model with the given name
     */
    public ArmorStandModel get( String name ) {
        for ( ArmorStandModel model : models ) {
            if ( model.getName().equals( name ) ) {
                return model;
            }
        }
        return null;
    }

    /**
     * Removes the model from the list
     *
     * @param name the name of the model to remove
     */
    public void remove( String name ) {
        models.remove( get( name ) );
    }

    /**
     * Removes a model from the list
     *
     * @param model the model to remove
     */
    public void remove( ArmorStandModel model ) {
        models.remove( model );
    }

    /**
     * @return a list with all loaded models
     */
    public List<ArmorStandModel> getModels() {
        return models;
    }
}
