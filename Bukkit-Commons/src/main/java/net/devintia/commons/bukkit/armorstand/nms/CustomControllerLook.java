package net.devintia.commons.bukkit.armorstand.nms;

import net.minecraft.server.v1_9_R1.ControllerLook;
import net.minecraft.server.v1_9_R1.EntityInsentient;

/**
 * Custom controller look, completely disables every look rotation
 */
class CustomControllerLook extends ControllerLook {

    private EntityInsentient a;

    CustomControllerLook( EntityInsentient entityInsentient ) {
        super( entityInsentient );
        this.a = entityInsentient;
    }

    @Override
    public void a() {
        //empty to completely disable look rotation
    }
}
