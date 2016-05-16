package net.devintia.commons.bukkit.armorstand;

import com.comphenix.packetwrapper.WrapperPlayClientSteerVehicle;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author MiniDigger
 * @version 1.0.0
 */
public class ArmorStandModelListener implements Listener {

    private Plugin plugin;
    private Map<UUID, Long> cooldDown = new HashMap<>();

    public ArmorStandModelListener( Plugin plugin, ArmorStandModelHandler handler ) {
        this.plugin = plugin;

        ProtocolLibrary.getProtocolManager().addPacketListener( new PacketAdapter( plugin, PacketType.Play.Client.STEER_VEHICLE ) {
            @Override
            public void onPacketReceiving( PacketEvent event ) {
                handleSteer( new WrapperPlayClientSteerVehicle( event.getPacket() ), handler, event.getPlayer() );
            }
        } );
    }

    @EventHandler
    public void onManipulate( PlayerArmorStandManipulateEvent e ) {
        if ( e.getRightClicked().hasMetadata( "armorstandmodel" ) ) {
            e.setCancelled( true );
        }
    }

    public void handleSteer( WrapperPlayClientSteerVehicle wrapped, ArmorStandModelHandler handler, Player player ) {
        if ( wrapped.isUnmount() ) {
            handler.setRiding( player, null );
            return;
        }

        if ( cooldDown.containsKey( player.getUniqueId() ) ) {
            if ( System.currentTimeMillis() > cooldDown.get( player.getUniqueId() ) + 0.1 * 1000 ) {
                cooldDown.remove( player.getUniqueId() );
            } else {
                return;
            }
        }

        cooldDown.put( player.getUniqueId(), System.currentTimeMillis() );

        System.out.println( "fw " + wrapped.getForward() + " sw " + wrapped.getSideways() + " j " + wrapped.isJump() + " um " + wrapped.isUnmount() );
        ArmorStandModel model = handler.getModel( player );
        if ( model != null ) {
            if ( wrapped.getSideways() > 0 ) {
                model.rotate( -10, plugin, null );
            } else if ( wrapped.getSideways() < 0 ) {
                model.rotate( 10, plugin, null );
            }
            if ( wrapped.getForward() > 0 ) {
                //TODO forward movement
            } else if ( wrapped.getForward() < 0 ) {
                //TODO backward movement
            }
        }
    }
}
