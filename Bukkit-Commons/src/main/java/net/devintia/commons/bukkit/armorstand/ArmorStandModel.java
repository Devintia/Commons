package net.devintia.commons.bukkit.armorstand;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Holds up multiple ArmrStandModelEntities.<br>
 * Can spawn, despawn (and in the future: move) the whole model
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class ArmorStandModel {

    private String name;
    private List<ArmorStandModelEntity> entities;

    ArmorStandModel( String name, List<ArmorStandModelEntity> entites ) {
        this.name = name;
        this.entities = entites;
    }

    /**
     * Spawns the model at the given location
     *
     * @param rootLocation the location to spawn the model
     * @param plugin       the plugin that spawns the model
     */
    public void spawn( Location rootLocation, Plugin plugin ) {
        for ( ArmorStandModelEntity entity : entities ) {
            entity.spawn( rootLocation, plugin );
        }
    }


    /**
     * Despawns this model
     */
    public void despawn() {
        for ( ArmorStandModelEntity entity : entities ) {
            entity.despawn();
        }
    }

    /**
     * @return the name of the model
     */
    public String getName() {
        return name;
    }

    /**
     * Moves to armorstand to the location
     *
     * @param loc    the location to move to
     * @param plugin the plugin that moves the model
     */
    public void move( Location loc, Plugin plugin ) {
        throw new NotImplementedException( "moving of armorstandsmodels is not implemented yet" );
    }
}
