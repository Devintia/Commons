package net.devintia.commons.bukkit.armorstand.nms;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_9_R1.EntityInsentient;
import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.Item;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import net.minecraft.server.v1_9_R1.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

/**
 * Collection of small nms methods
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class NMSUtil {

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

    public static ItemStack getItemStack( String mat ) {
        MinecraftKey mk = new MinecraftKey( mat );
        return CraftItemStack.asNewCraftStack( Item.REGISTRY.get( mk ) );
    }

    public static CraftArmorStand spawnArmorStand( Location loc, Plugin plugin ) {
        CraftWorld cw = ( (CraftWorld) loc.getWorld() );
        NoGravityArmorStand a = new NoGravityArmorStand( cw.getHandle(), loc.getX(), loc.getY(), loc.getZ() );
        cw.getHandle().addEntity( a, CreatureSpawnEvent.SpawnReason.CUSTOM );
        return new CraftArmorStand( (CraftServer) Bukkit.getServer(), a );
    }

    public static void clearPathFinding( LivingEntity entity ) {
        EntityInsentient entityInsentient = (EntityInsentient) ( (CraftLivingEntity) entity ).getHandle();
        try {
            bField.set( entityInsentient.goalSelector, Sets.newLinkedHashSet() );
            bField.set( entityInsentient.targetSelector, Sets.newLinkedHashSet() );
            cField.set( entityInsentient.goalSelector, Sets.newLinkedHashSet() );
            cField.set( entityInsentient.targetSelector, Sets.newLinkedHashSet() );
            look.set( entityInsentient, new CustomControllerLook( entityInsentient ) );
        } catch ( Exception exc ) {
            exc.printStackTrace();
        }
    }

    public static void setPitchYaw( LivingEntity entity, float pitch, float yaw ) {
        EntityLiving nmsEntity = ( (CraftLivingEntity) entity ).getHandle();
        //yaw
        nmsEntity.yaw = yaw;
        nmsEntity.lastYaw = yaw;
        nmsEntity.aO = yaw;
        nmsEntity.aM = yaw;
        //pitch
        nmsEntity.pitch = pitch;
        nmsEntity.lastPitch = pitch;
    }

    public static void setSilent( LivingEntity entity ) {
        ( (CraftLivingEntity) entity ).getHandle().b( true );
    }

    public static void setNoClip( LivingEntity entity ) {
        ( (CraftLivingEntity) entity ).getHandle().noclip = true;
    }
}
