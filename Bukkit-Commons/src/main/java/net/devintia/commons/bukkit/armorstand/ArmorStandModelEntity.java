package net.devintia.commons.bukkit.armorstand;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.EntityInsentient;
import net.minecraft.server.v1_9_R1.EntityVillager;
import net.minecraft.server.v1_9_R1.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftVillager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

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

    //fields
    private static Field bField;
    private static Field cField;
    private static Field look;

    static {
        try {
            bField = PathfinderGoalSelector.class.getDeclaredField( "b" );
            bField.setAccessible( true );

            cField = PathfinderGoalSelector.class.getDeclaredField( "c" );
            cField.setAccessible( true );

            look = EntityInsentient.class.getDeclaredField( "lookController" );
            look.setAccessible( true );
        } catch ( NoSuchFieldException e ) {
            e.printStackTrace();
        }
    }

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

                armorStand = spawnArmorStand( loc, plugin );
                armorStand.setVisible( false );
                armorStand.setAI( false );

                villager = (Villager) loc.getWorld().spawnEntity( loc, EntityType.VILLAGER );
                EntityVillager entityVillager = ( (CraftVillager) villager ).getHandle();
                entityVillager.yaw = loc.getYaw();
                entityVillager.pitch = loc.getPitch();
                clearPathFinding( entityVillager );
                entityVillager.getControllerLook().a( loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw() );
                entityVillager.setPositionRotation( loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw() );
                entityVillager.noclip = true;
                entityVillager.b( true );//silent
                entityVillager.collides = false;
                //TODO test which ones we realy need
                entityVillager.yaw = loc.getYaw();
                entityVillager.pitch = loc.getPitch();
                entityVillager.lastPitch = loc.getPitch();
                entityVillager.lastYaw = loc.getYaw();
                entityVillager.aO = loc.getYaw();
                entityVillager.aM = loc.getYaw();
                villager.setBaby();
                villager.addPotionEffect( new PotionEffect( PotionEffectType.INVISIBILITY, 9999999, 255, false, false ) );
                villager.setInvulnerable( true );
                villager.getEquipment().setHelmet( item );
                armorStand.setPassenger( villager );
                break;
            case MEDIUM:
                armorStand = spawnArmorStand( loc, plugin );
                armorStand.setSmall( true );
                armorStand.setVisible( false );
                armorStand.setAI( false );
                armorStand.setCustomName( customName );
                armorStand.getEquipment().setHelmet( item );
                armorStand.setHeadPose( new EulerAngle( Math.toRadians( headPose.getX() ), Math.toRadians( headPose.getY() ), Math.toRadians( headPose.getZ() ) ) );
                ( (NoGravityArmorStand) ( (CraftArmorStand) armorStand ).getHandle() ).update();
                break;
            case LARGE:
                armorStand = spawnArmorStand( loc, plugin );
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
                System.out.println( loc.getBlock().getType() );
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
            EntityArmorStand entity = ( (CraftArmorStand) armorStand ).getHandle();
            entity.yaw = rotation;
            entity.lastYaw = rotation;
            entity.aO = rotation;
            entity.aM = rotation;
        }
        if ( villager != null ) {
            EntityVillager entity = ( (CraftVillager) villager ).getHandle();
            entity.yaw = rotation;
            entity.lastYaw = rotation;
            entity.aO = rotation;
            entity.aM = rotation;
        }
    }

    private CraftArmorStand spawnArmorStand( Location loc, Plugin plugin ) {
        CraftWorld cw = ( (CraftWorld) loc.getWorld() );
        NoGravityArmorStand a = new NoGravityArmorStand( cw.getHandle(), loc.getX(), loc.getY(), loc.getZ() );
        cw.getHandle().addEntity( a, CreatureSpawnEvent.SpawnReason.CUSTOM );
        return new CraftArmorStand( (CraftServer) Bukkit.getServer(), a );
    }

    private void clearPathFinding( EntityInsentient entity ) {
        try {
            bField.set( entity.goalSelector, Sets.newLinkedHashSet() );
            bField.set( entity.targetSelector, Sets.newLinkedHashSet() );
            cField.set( entity.goalSelector, Sets.newLinkedHashSet() );
            cField.set( entity.targetSelector, Sets.newLinkedHashSet() );
            look.set( entity, new CustomControllerLook( entity ) );
        } catch ( Exception exc ) {
            exc.printStackTrace();
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

    public Entity getEntity() {
        return armorStand;
    }
}
