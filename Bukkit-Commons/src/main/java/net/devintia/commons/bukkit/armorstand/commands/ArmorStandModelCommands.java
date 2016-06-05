package net.devintia.commons.bukkit.armorstand.commands;

import net.devintia.commons.bukkit.TestPlugin;
import net.devintia.commons.bukkit.armorstand.ArmorStandModel;
import net.devintia.commons.bukkit.armorstand.ArmorStandModelImporter;
import net.devintia.commons.bukkit.command.CommandArguments;
import net.devintia.commons.bukkit.command.CommandInfo;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.io.File;

/**
 * Created by Martin on 05.06.2016.
 */
public class ArmorStandModelCommands {

    private TestPlugin plugin;

    public ArmorStandModelCommands( TestPlugin plugin ) {
        this.plugin = plugin;
    }

    @CommandInfo( name = "loadmodel", perm = "loadmodel", description = "Loads and spawns a armor stand model", usage = "<command> <filename>", allowConsole = false )
    public void loadModel( CommandArguments args ) {
        if ( args.getNumArgs() < 1 ) {
            args.getSender().sendMessage( ChatColor.RED + "Usage: " + args.getCommand().getUsage() );
            return;
        }

        String name = args.getArg( 0 );
        if ( !name.endsWith( ".model" ) ) {
            name += ".model";
        }

        File file = new File( plugin.getDataFolder(), name );
        if ( !file.exists() ) {
            args.getSender().sendMessage( "unknown model: " + name );
            return;
        }

        String display = file.getName();
        if ( args.getNumArgs() >= 2 ) {
            display = args.getArg( 1 );
        }

        if ( display.endsWith( ".model" ) ) {
            display = display.replace( ".model", "" );
        }

        ArmorStandModel model = ArmorStandModelImporter.importModel( display, file );

        if ( model == null ) {
            args.getSender().sendMessage( "error while loading model: " + display );
            return;
        }

        Location loc = new Location( args.getPlayer().getWorld(), args.getPlayer().getLocation().getX(), args.getPlayer().getLocation().getY(), args.getPlayer().getLocation().getZ() );
        model.spawn( loc, plugin );

        args.getSender().sendMessage( "spawned: " + display );
    }

    @CommandInfo( name = "movemodel", perm = "movemodel", allowConsole = false )
    public void moveModel( CommandArguments args ) {
        if ( args.getNumArgs() != 1 ) {
            args.getSender().sendMessage( "/movemodel <model>" );
        }

        ArmorStandModel model = plugin.getArmorStandModelHandler().get( args.getArg( 0 ) );
        model.move( args.getPlayer().getLocation(), plugin, 0.5f, () -> System.out.println( "moved : " + args.getArg( 0 ) ) );
    }
}
