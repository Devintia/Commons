package net.devintia.commons.bukkit.armorstand;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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
    private Location rootLocation;
    private boolean moving = false;

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
        this.rootLocation = rootLocation;

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
     * Moves to armorstand to the location<br>
     * <b>DO NOT USE IF MODEL CONTAINS SOLID BLOCKS!</b><br>
     * Can't be moved <code>if(isMoving())</code>
     *
     * @param loc      the location to move to
     * @param plugin   the plugin that moves the model
     * @param speed    the speed the model should move with (in blocks/tick)
     * @param callBack a callback that gets executed if the move was finished, may be null
     */
    public void move( Location loc, Plugin plugin, float speed, Runnable callBack ) {
        if ( rootLocation == null ) {
            throw new IllegalStateException( "Model need to be spawned before it can be moved!" );
        }

        if ( isMoving() ) {
            throw new IllegalStateException( "Can't be moved if currently moving" );
        }

        Vector velo = loc.toVector().subtract( rootLocation.toVector() ).normalize().multiply( speed );

        for ( ArmorStandModelEntity entity : entities ) {
            entity.move( velo );
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                rootLocation.add( velo );
                if ( loc.distance( rootLocation ) < 1 ) {
                    for ( ArmorStandModelEntity entity : entities ) {
                        entity.move( new Vector( 0, 0, 0 ) );
                    }
                    if ( callBack != null ) {
                        callBack.run();
                    }
                    cancel();
                }
            }
        }.runTaskTimer( plugin, 1, 1 );
    }

    /**
     * @return weather or not this model is currently moving
     */
    public boolean isMoving() {
        return moving;
    }

    /**
     * Rotates the model
     *
     * @param degrees  the number of degrees the model should be rotated
     * @param plugin   the plugin that rotates the model
     * @param callBack a callback that gets executed if the move was finished, may be null
     */
    public void rotate( float degrees, Plugin plugin, Runnable callBack ) {
        for ( ArmorStandModelEntity entity : entities ) {
            Vector loc = entity.getLocation();
            Vector rot = rotate( loc, (float) Math.toRadians( degrees ) );
            Vector velo = rot.clone().subtract( loc );
            entity.move( velo );

            entity.rotate( degrees );
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for ( ArmorStandModelEntity entity : entities ) {
                    entity.move( new Vector( 0, 0, 0 ) );
                }
                callBack.run();
            }
        }.runTaskLater( plugin, 2 );
    }

    private Vector rotate( Vector vector, float theta ) {
        double x = ( vector.getX() * Math.cos( theta ) ) - ( vector.getZ() * Math.sin( theta ) );
        double z = ( vector.getX() * Math.sin( theta ) ) - ( vector.getZ() * Math.cos( theta ) );

        return new Vector( x, vector.getY(), z );
    }
}
