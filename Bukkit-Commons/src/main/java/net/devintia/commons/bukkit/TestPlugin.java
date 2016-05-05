package net.devintia.commons.bukkit;

import net.devintia.commons.bukkit.command.CommandArguments;
import net.devintia.commons.bukkit.command.CommandHandler;
import net.devintia.commons.bukkit.command.CommandInfo;
import net.devintia.commons.bukkit.command.CommandUtil;
import net.devintia.commons.bukkit.command.CompleterInfo;
import org.bukkit.plugin.java.JavaPlugin;

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

    @Override
    public void onEnable() {
        cmdHandler = new CommandHandler( this );
        cmdHandler.register( this );
        cmdHandler.registerHelp();
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
}