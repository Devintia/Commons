package net.devintia.commons.bukkit.armorstand.commands;

import net.devintia.commons.bukkit.armorstand.ArmorStandModel;
import net.devintia.commons.bukkit.armorstand.ArmorStandModelHandler;
import net.devintia.commons.bukkit.armorstand.ArmorStandModelImporter;
import net.devintia.commons.bukkit.command.CommandArguments;
import net.devintia.commons.bukkit.command.CommandInfo;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * @author MiniDigger
 * @version 1.0.0
 */
public class ArmorStandModelCommands {

    private ArmorStandModelHandler armorStandModelHandler;
    private Plugin plugin;

    public ArmorStandModelCommands( ArmorStandModelHandler armorStandModelHandler, Plugin plugin ) {
        this.armorStandModelHandler = armorStandModelHandler;
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
            display.replace( ".model", "" );
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

        ArmorStandModel model = armorStandModelHandler.get( args.getArg( 0 ) );
        if ( model == null ) {
            args.getSender().sendMessage( "unknown model " + args.getArg( 0 ) );
            return;
        }

        model.move( args.getPlayer().getLocation(), plugin, 0.5f, null );
    }


    @CommandInfo( name = "rotatemodel", perm = "roatemodel", allowConsole = false )
    public void rotateModel( CommandArguments args ) {
        if ( args.getNumArgs() < 1 ) {
            args.getSender().sendMessage( "/rotatemodel <model> [degrees]" );
        }

        ArmorStandModel model = armorStandModelHandler.get( args.getArg( 0 ) );
        if ( model == null ) {
            args.getSender().sendMessage( "unknown model " + args.getArg( 0 ) );
            return;
        }

        if ( args.getNumArgs() == 1 ) {
            new BukkitRunnable() {
                int count = 1;
                int deg = 5;

                @Override
                public void run() {
                    model.rotate( deg, plugin, null );
                    count += deg;
                    if ( count > 360 ) {
                        cancel();
                    }
                }
            }.runTaskTimer( plugin, 1, 1 );
            return;
        }

        float degrees;
        try {
            degrees = Float.parseFloat( args.getArg( 1 ) );
        } catch ( NumberFormatException ex ) {
            args.getSender().sendMessage( "Invalid number " + args.getArg( 1 ) );
            return;
        }

        model.rotate( degrees, plugin, null );
    }


    @CommandInfo( name = "followmodel", perm = "followmodel", allowConsole = false )
    public void followmodel( CommandArguments args ) {
        if ( args.getNumArgs() < 1 ) {
            args.getSender().sendMessage( "/followmodel <model>" );
        }

        ArmorStandModel model = armorStandModelHandler.get( args.getArg( 0 ) );
        if ( model == null ) {
            args.getSender().sendMessage( "unknown model " + args.getArg( 0 ) );
            return;
        }

        model.rotateAndMoveTo( args.getPlayer(), plugin );
    }


    @CommandInfo( name = "ridemodel", perm = "ridemodel", allowConsole = false )
    public void ridemodel( CommandArguments args ) {
        if ( args.getNumArgs() < 1 ) {
            args.getSender().sendMessage( "/ridemodel <model>" );
        }

        // TODO we need a designated "seat"
        ArmorStandModel model = armorStandModelHandler.get( args.getArg( 0 ) );
        if ( model == null ) {
            args.getSender().sendMessage( "unknown model " + args.getArg( 0 ) );
            return;
        }

        model.addPassagner( args.getPlayer() );
    }
}
