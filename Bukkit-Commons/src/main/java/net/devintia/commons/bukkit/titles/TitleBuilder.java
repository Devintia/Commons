package net.devintia.commons.bukkit.titles;

import net.minecraft.server.v1_10_R1.ChatComponentText;
import net.minecraft.server.v1_10_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import static com.google.common.base.Preconditions.*;

/**
 * Simple ChainAble builder to create and send title to players.
 * Example usage:
 * <code>new TitleBuilder().title("Title").subtitle("Subtitle").times(2,2,2).send(player)</code>
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class TitleBuilder {

    private String title;
    private String subTitle;
    private int[] times;


    /**
     * Sets the title message for this title
     *
     * @param title The new title message
     * @return this builder for chaining
     */
    public TitleBuilder title( String title ) {
        checkNotNull( title );

        this.title = title;
        return this;
    }

    /**
     * Sets the subtitle message for this title
     *
     * @param subTitle The new title message
     * @return this builder for chaining
     */
    public TitleBuilder subtitle( String subTitle ) {
        checkNotNull( subTitle );

        this.subTitle = subTitle;
        return this;
    }

    /**
     * Sets the timings for this title
     *
     * @param fadeIn  time in seconds the title fades in
     * @param stay    time in seconds the title stay visible
     * @param fadeOut time in seconds the title fades out
     * @return this builder for chaining
     */
    public TitleBuilder times( int fadeIn, int stay, int fadeOut ) {
        checkArgument( fadeIn >= 0 );
        checkArgument( stay >= 0 );
        checkArgument( fadeOut >= 0 );

        times = new int[]{ fadeIn, stay, fadeOut };
        return this;
    }


    /**
     * Sends the title to the specified player
     *
     * @param player The player to send the title to
     */
    public void send( Player player ) {
        checkArgument( player instanceof CraftPlayer );
        checkState( title != null );
        checkState( times != null );

        CraftPlayer cp = (CraftPlayer) player;
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle( PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText( title ), times[0], times[1], times[2] );
        if ( subTitle != null ) {
            PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle( PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText( subTitle ) );
            cp.getHandle().playerConnection.sendPacket( subtitlePacket );
        }
        cp.getHandle().playerConnection.sendPacket( titlePacket );
    }

    /**
     * Sends the title to all online players
     */
    public void sendAll() {
        Bukkit.getOnlinePlayers().forEach( this::send );
    }
}