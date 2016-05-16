package net.devintia.commons.bukkit.armorstand;

import net.devintia.commons.bukkit.armorstand.nms.NMSUtil;
import net.devintia.commons.bukkit.armorstand.nms.NoGravityArmorStand;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * @author MiniDigger
 * @version 1.0.0
 */
class ArmorStandModelEntity {

    // data
    private ItemStack item;
    private ArmorStandModelSize size;
    private Vector location;
    private String customName;
    private Vector headPose;

    // entities and blocks
    private BlockState blockState;
    private ArmorStand armorStand;
    private Villager villager;

    private float rotation;

    ArmorStandModelEntity( ItemStack item, ArmorStandModelSize size, String customName, Vector headPose ) {
        this.item = item;
        this.size = size;
        this.customName = customName;
        this.headPose = headPose;
    }

    void setLocation( Vector location ) {
        this.location = location;
    }

    void spawn( Location rootLocation, Plugin plugin ) {
        Location loc = new Location( rootLocation.getWorld(), rootLocation.getX() + location.getX(), rootLocation.getY() + location.getY(), rootLocation.getZ() + location.getZ() );

        switch ( size ) {
            case SMALL:
                loc.setY( loc.getY() - 1.5 );
                loc.setPitch( (float) headPose.getY() );
                loc.setYaw( (float) headPose.getX() );

                armorStand = NMSUtil.spawnArmorStand( loc, plugin );
                armorStand.setVisible( false );
                armorStand.setAI( false );

                villager = (Villager) loc.getWorld().spawnEntity( loc, EntityType.VILLAGER );
                NMSUtil.clearPathFinding( villager );
                NMSUtil.setNoClip( villager );
                NMSUtil.setSilent( villager );
                NMSUtil.setPitchYaw( villager, loc.getPitch(), loc.getYaw() );
                villager.setBaby();
                villager.addPotionEffect( new PotionEffect( PotionEffectType.INVISIBILITY, 9999999, 255, false, false ) );
                villager.setInvulnerable( true );
                villager.getEquipment().setHelmet( item );
                armorStand.setPassenger( villager );
                break;
            case MEDIUM:
                armorStand = NMSUtil.spawnArmorStand( loc, plugin );
                armorStand.setSmall( true );
                armorStand.setVisible( false );
                armorStand.setAI( false );
                armorStand.setCustomName( customName );
                armorStand.getEquipment().setHelmet( item );
                armorStand.setHeadPose( new EulerAngle( Math.toRadians( headPose.getX() ), Math.toRadians( headPose.getY() ), Math.toRadians( headPose.getZ() ) ) );
                ( (NoGravityArmorStand) ( (CraftArmorStand) armorStand ).getHandle() ).update();
                break;
            case LARGE:
                armorStand = NMSUtil.spawnArmorStand( loc, plugin );
                armorStand.setVisible( false );
                armorStand.setAI( false );
                armorStand.setCustomName( customName );
                armorStand.getEquipment().setHelmet( item );
                armorStand.setHeadPose( new EulerAngle( Math.toRadians( headPose.getX() ), Math.toRadians( headPose.getY() ), Math.toRadians( headPose.getZ() ) ) );
                ( (NoGravityArmorStand) ( (CraftArmorStand) armorStand ).getHandle() ).update();
                break;
            case SOLID:
                blockState = loc.getBlock().getState();
                loc.getBlock().setTypeIdAndData( item.getTypeId(), (byte) item.getDurability(), false );
                break;
        }
    }

    void despawn() {
        if ( blockState != null ) {
            blockState.update( true, false );
        }
        if ( armorStand != null ) {
            armorStand.remove();
        }
        if ( villager != null ) {
            villager.remove();
        }
    }

    void move( Vector velo ) {
        if ( armorStand != null ) {
            armorStand.setVelocity( velo );
        }
        if ( villager != null ) {
            villager.setVelocity( velo );
        }
        location.add( velo );
    }

    void rotate( float rad ) {
        rotation = ( rotation + rad ) % 360;
        if ( armorStand != null ) {
            NMSUtil.setPitchYaw( armorStand, armorStand.getLocation().getPitch(), rotation );
        }
        if ( villager != null ) {
            NMSUtil.setPitchYaw( villager, villager.getLocation().getPitch(), rotation );
        }
    }

    Vector getLocation() {
        return location;
    }

    void teleport( Location rootLocation, Vector newLocation ) {
        if ( newLocation != null ) {
            location = newLocation;
        }
        Location newLoc = new Location( rootLocation.getWorld(), rootLocation.getX() + location.getX(), rootLocation.getY() + location.getY(), rootLocation.getZ() + location.getZ() );
        if ( armorStand != null ) {
            armorStand.teleport( newLoc );
        }
        if ( villager != null ) {
            villager.teleport( newLoc );
        }
    }

    Entity getEntity() {
        return armorStand;
    }
}
