package net.devintia.commons.bukkit.armorstand;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to store all loaded models to keep track of them and remove them on disable
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class ArmorStandModelHandler {

    private List<ArmorStandModel> models = new ArrayList<>();
    private Map<UUID, String> riding = new HashMap<>();

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
        checkNotNull( model );

        models.add( model );
    }

    /**
     * @param name the name of the model
     * @return the reference to the model with the given name
     */
    public ArmorStandModel get( String name ) {
        checkNotNull( name );

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
        checkNotNull( name );

        models.remove( get( name ) );
    }

    /**
     * Removes a model from the list
     *
     * @param model the model to remove
     */
    public void remove( ArmorStandModel model ) {
        checkNotNull( model );

        models.remove( model );
    }

    /**
     * @return a list with all loaded models
     */
    public List<ArmorStandModel> getModels() {
        return models;
    }

    /**
     * Notifies this handler that player is now mounted to model
     *
     * @param player the player who is riding the model
     * @param model  the model the player is riding
     */
    public void setRiding( Player player, ArmorStandModel model ) {
        checkNotNull( player );

        if ( model == null ) {
            riding.remove( player.getUniqueId() );
        } else {
            riding.put( player.getUniqueId(), model.getName() );
        }
    }

    /**
     * Returns the model the player is riding, could be null
     *
     * @param player the player
     * @return the models the player is riding, could be null
     */
    public ArmorStandModel getModel( Player player ) {
        String name = riding.get( player.getUniqueId() );
        if ( name != null ) {
            ArmorStandModel model = get( name );
            if ( model != null ) {
                return model;
            }
        }
        return null;
    }
}
