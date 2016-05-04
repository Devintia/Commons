package net.devintia.commons.bukkit.titles;

import net.minecraft.server.v1_9_R1.ChatComponentText;
import net.minecraft.server.v1_9_R1.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TitleBuilder {

    private String title;
    private String subTitle;
    private int[] times;

    public TitleBuilder title( String title ) {
        this.title = title;
        return this;
    }

    public TitleBuilder subtitle( String subTitle ) {
        this.subTitle = subTitle;
        return this;
    }

    public TitleBuilder times( int fadeIn, int stay, int fadeOut ) {
        times = new int[]{ fadeIn, stay, fadeOut };
        return this;
    }

    public void send( Player player ) {
        if ( !( player instanceof CraftPlayer ) ) {
            throw new IllegalArgumentException( "Specified Player needs to be an instance of CraftPlayer!" );
        }
        CraftPlayer cp = (CraftPlayer) player;
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle( PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText( title ), times[0], times[1], times[2] );
        if ( subTitle != null ) {
            PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle( PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText( subTitle ) );
            cp.getHandle().playerConnection.sendPacket( subtitlePacket );
        }
        cp.getHandle().playerConnection.sendPacket( titlePacket );
    }
}