package net.devintia.commons.bukkit.armorstand.nms;

import net.minecraft.server.v1_9_R1.ControllerLook;
import net.minecraft.server.v1_9_R1.EntityInsentient;

/**
 * Custom controller look, completely disables every look rotation
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class CustomControllerLook extends ControllerLook {

    private EntityInsentient a;

    public CustomControllerLook( EntityInsentient entityInsentient ) {
        super( entityInsentient );
        this.a = entityInsentient;
    }

    @Override
    public void a() {
        //empty to completely disable look rotation
    }
}
