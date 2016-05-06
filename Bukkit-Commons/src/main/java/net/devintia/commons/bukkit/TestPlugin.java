package net.devintia.commons.bukkit;

import net.devintia.commons.bukkit.command.CommandArguments;
import net.devintia.commons.bukkit.command.CommandHandler;
import net.devintia.commons.bukkit.command.CommandInfo;
import net.devintia.commons.bukkit.command.CommandUtil;
import net.devintia.commons.bukkit.command.CompleterInfo;
import net.devintia.commons.bukkit.armorstand.ArmorStandModel;
import net.devintia.commons.bukkit.armorstand.ArmorStandModelHandler;
import net.devintia.commons.bukkit.armorstand.ArmorStandModelImporter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JavaPlugin, used to test stuff
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class TestPlugin extends JavaPlugin {

    private CommandHandler cmdHandler;
    private ArmorStandModelHandler armorStandModelHandler;

    @Override
    public void onEnable() {
        cmdHandler = new CommandHandler( this );
        cmdHandler.register( this );
        cmdHandler.registerHelp();

        armorStandModelHandler = new ArmorStandModelHandler();
        ArmorStandModelImporter.setHandler( armorStandModelHandler );
    }

    @Override
    public void onDisable() {
        armorStandModelHandler.disable();
    }

    @CommandInfo( name = "testcommand", perm = "testcommand", description = "Test Command to test arguments", usage = "<command> [sub/args]" )
    public void testCommand( CommandArguments args ) {
        args.getSender().sendMessage( Arrays.toString( args.getArgs() ) );
    }

    @CommandInfo( name = "testcommand.sub", perm = "testcommand.sub" )
    public void testCommandSub( CommandArguments args ) {
        args.getSender().sendMessage( "sub: " + Arrays.toString( args.getArgs() ) );
    }

    @CommandInfo( name = "testcommand.sub.sub", perm = "testcommand.sub.sub" )
    public void testCommandSubSub( CommandArguments args ) {
        args.getSender().sendMessage( "subsub: " + Arrays.toString( args.getArgs() ) );
    }

    @CompleterInfo( name = "testcommand" )
    public List<String> testCommandCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            result.add( "sub" );
            result.add( "sab" );
            result.add( "abb" );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }

    @CompleterInfo( name = "testcommand.sub" )
    public List<String> testCommandSubCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            result.add( "sub" );
            result.add( "sab" );

            CommandUtil.addOnlinePlayerNames( result );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }

    @CommandInfo( name = "unregister", perm = "unregister", description = "Unregisters a command", usage = "<command> <command to unregister>" )
    public void unregister( CommandArguments args ) {
        if ( args.getNumArgs() != 1 ) {
            args.getSender().sendMessage( args.getCommand().getUsage() );
            return;
        }

        cmdHandler.unregisterCommand( args.getArg( 0 ) );
    }

    @CommandInfo( name = "loadmodel", perm = "loadmodel", description = "Loads and spawns a armor stand model", usage = "<command> <filename>", allowConsole = false )
    public void loadModel( CommandArguments args ) {
        if ( args.getNumArgs() != 1 ) {
            args.getSender().sendMessage( ChatColor.RED + "Usage: " + args.getCommand().getUsage() );
            return;
        }

        String name = args.getArg( 0 );
        if ( !name.endsWith( ".model" ) ) {
            name += ".model";
        }

        File file = new File( getDataFolder(), name );
        if ( !file.exists() ) {
            args.getSender().sendMessage( "unknown model: " + name );
            return;
        }

        String display = file.getName();
        if ( display.endsWith( ".model" ) ) {
            display.replace( ".model", "" );
        }

        ArmorStandModel model = ArmorStandModelImporter.importModel( display, file );

        if ( model == null ) {
            args.getSender().sendMessage( "error while loading model: " + display );
            return;
        }

        Location loc = new Location( args.getPlayer().getWorld(), args.getPlayer().getLocation().getBlockX() + 0.5, args.getPlayer().getLocation().getBlockY(), args.getPlayer().getLocation().getBlockZ() + 0.5 );
        model.spawn( loc, this );

        args.getSender().sendMessage( "done" );
    }

    @CompleterInfo( name = "loadmodel" )
    public List<String> loadModelCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            String[] names = getDataFolder().list( ( dir, name ) -> name.endsWith( ".model" ) );

            result.addAll( Arrays.asList( names ) );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }
}
