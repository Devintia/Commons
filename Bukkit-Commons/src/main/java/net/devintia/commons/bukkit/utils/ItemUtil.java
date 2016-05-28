package net.devintia.commons.bukkit.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftMetaBook;
import org.bukkit.inventory.ItemStack;

import static com.google.common.base.Preconditions.*;

/**
 * Collection of useful methods for items
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class ItemUtil {

    /**
     * Adds a new page to the book
     *
     * @param book the book to add the page too
     * @param page the page to add
     */
    public static void addPageToBook( ItemStack book, BaseComponent[] page ) {
        checkNotNull( book );
        checkArgument( book.hasItemMeta() );

        CraftMetaBook meta = (CraftMetaBook) book.getItemMeta();
        meta.pages.add( IChatBaseComponent.ChatSerializer.a( ComponentSerializer.toString( page ) ) );
        book.setItemMeta( meta );
    }
}
