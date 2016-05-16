package net.devintia.commons.bukkit.armorstand;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

/**
 * @author MiniDigger
 * @version 1.0.0
 */
public class ArmorStandModelListener implements Listener {

    @EventHandler
    public void onManipulate( PlayerArmorStandManipulateEvent e ) {
        if ( e.getRightClicked().hasMetadata( "armorstandmodel" ) ) {
            e.setCancelled( true );
        }
    }
}
