package net.devintia.commons.bukkit.armorstand;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
    private float rotation;
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

        new BukkitRunnable() {
            @Override
            public void run() {
                rootLocation.getWorld().playEffect( rootLocation, Effect.COLOURED_DUST, 0x2 );
            }
        }.runTaskTimer( plugin, 1, 1 );
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
        checkNotNull( loc );
        checkNotNull( plugin );
        checkNotNull( rootLocation, "Model needs to be spawned before it can be moved!" );
        checkArgument( !isMoving(), "Can't be moved if currently moving" );

        moving = true;

        Vector velo = loc.toVector().subtract( rootLocation.toVector() ).normalize().multiply( speed );

        for ( ArmorStandModelEntity entity : entities ) {
            entity.move( velo );
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                double distance = loc.distance( rootLocation );
                if ( distance < 1 ) {
                    for ( ArmorStandModelEntity entity : entities ) {
                        entity.move( new Vector( 0, 0, 0 ) );
                    }

                    moving = false;

                    if ( callBack != null ) {
                        callBack.run();
                    }

                    cancel();
                } else {
                    rootLocation.add( velo );
                }
            }
        }.runTaskTimer( plugin, 0, 1 );
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
        checkNotNull( plugin );
        checkArgument( degrees >= -360 && degrees <= 360 );

        this.rotation = ( rotation + degrees ) % 360;

        for ( ArmorStandModelEntity entity : entities ) {
            Vector loc = entity.getLocation();
            Vector rot = rotate( loc, (float) Math.toRadians( degrees ) );
            Vector velo = rot.clone().subtract( loc );
            entity.rotate( degrees );
            entity.move( velo );
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for ( ArmorStandModelEntity entity : entities ) {
                    entity.move( new Vector( 0, 0, 0 ) );
                }

                if ( callBack != null ) {
                    callBack.run();
                }
            }
        }.runTaskLater( plugin, 2 );
    }

    private Vector rotate( Vector vector, float theta ) {
        double x = ( vector.getX() * Math.cos( theta ) ) - ( vector.getZ() * Math.sin( theta ) );
        double z = ( vector.getX() * Math.sin( theta ) ) + ( vector.getZ() * Math.cos( theta ) );

        return new Vector( x, vector.getY(), z );
    }

    /**
     * Make the model look at a location
     *
     * @param plugin   the plugin that will execute the rotation
     * @param loc      the location to look at
     * @param callBack a callback that gets executed if the move was finished, may be null
     */
    public void lookAt( Plugin plugin, Location loc, Runnable callBack ) {
        checkNotNull( plugin );
        checkNotNull( loc );

        float angle = getLocalAngle( rootLocation.toVector(), loc.toVector() ) - rotation;
        rotate( angle, plugin, callBack );
    }

    private float getLocalAngle( Vector point1, Vector point2 ) {
        double dx = point2.getX() - point1.getX();
        double dz = point2.getZ() - point1.getZ();
        float angle = (float) Math.toDegrees( Math.atan2( dz, dx ) ) - 90;
        if ( angle < 0 ) {
            angle += 360.0F;
        }
        return angle;
    }

    /**
     * Rotates the model and lets it move to the player
     *
     * @param p      the player to move to
     * @param plugin the plugin that executes the move
     */
    public void rotateAndMoveTo( Player p, Plugin plugin ) {
        checkNotNull( p );
        checkNotNull( plugin );

        lookAt( plugin, p.getLocation(), () -> move( p.getLocation(), plugin, 0.4f, () -> new BukkitRunnable() {
            @Override
            public void run() {
                //rotateAndMoveTo( p, plugin );
            }
        }.runTaskLater( plugin, 2 ) ) );
    }

    public void addPassagner( Entity e ) {
        checkNotNull( e );
        checkArgument( !e.isDead() );
        //TODO passagner stuff
        entities.get( 0 ).getEntity().setPassenger( e );
    }
}
