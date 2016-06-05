package net.devintia.commons.bukkit.armorstand.nms;

import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_9_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * A custom armorstand that does not respect gravity.<br>
 * Needs to be updated using the update method if moved!
 *
 * @author MiniDigger
 * @version 1.0.0
 */
class NoGravityArmorStand extends EntityArmorStand {

    NoGravityArmorStand( World world, double d0, double d1, double d2) {
        super( world, d0, d1, d2 );
        this.noclip = true;
    }

    /**
     * Notifies the players about changes to this armorstand
     */
    void update() {
        getDataWatcher();
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata( getId(), getDataWatcher(), false );
        for ( Player p : Bukkit.getOnlinePlayers() ) {
            ( (CraftPlayer) p ).getHandle().playerConnection.sendPacket( packet );
        }

        PacketPlayOutEntityTeleport packet1 = new PacketPlayOutEntityTeleport( NoGravityArmorStand.this );
        for ( final Player p : Bukkit.getOnlinePlayers() ) {
            ( (CraftPlayer) p ).getHandle().playerConnection.sendPacket( packet1 );
        }
    }

    @Override
    public void n() {
        // ignore all changes to the mot
        double motX = this.motX, motY = this.motY, motZ = this.motZ;
        super.n();
        this.motX = motX;
        this.motY = motY;
        this.motZ = motZ;
    }
}
