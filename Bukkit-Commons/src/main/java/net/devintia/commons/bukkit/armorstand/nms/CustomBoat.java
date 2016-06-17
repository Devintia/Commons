package net.devintia.commons.bukkit.armorstand.nms;

import net.minecraft.server.v1_10_R1.EntityBoat;
import net.minecraft.server.v1_10_R1.World;
import org.bukkit.Location;
import org.bukkit.entity.Vehicle;

/**
 * Created by Martin on 16.05.2016.
 */
public class CustomBoat extends EntityBoat {

    private Location lastLoc;

    public CustomBoat( World world, double d0, double d1, double d2 ) {
        super( world, d0, d1, d2 );
    }

    @Override
    public void m() {
        final org.bukkit.World bworld = this.world.getWorld();
        final Location to = new Location( bworld, this.locX, this.locY, this.locZ, this.yaw, this.pitch );
        final Vehicle vehicle = (Vehicle) this.getBukkitEntity();
        if ( lastLoc != null && !equals( lastLoc, to ) ) {
            System.out.println( "moved" );
        }
        lastLoc = vehicle.getLocation();
    }

    private boolean equals( Location loc1, Location loc2 ) {
        if ( Math.abs( loc1.getX() - loc2.getX() ) > 0.005 ) {
            return false;
        }
        if ( Math.abs( loc1.getY() - loc2.getY() ) > 0.005 ) {
            return false;
        }
        if ( Math.abs( loc1.getZ() - loc2.getZ() ) > 0.005 ) {
            return false;
        }
        if ( Math.abs( loc1.getPitch() - loc2.getPitch() ) > 0.05 ) {
            return false;
        }
        if ( Math.abs( loc1.getYaw() - loc2.getYaw() ) > 0.05 ) {
            return false;
        }
        return true;
    }
}
