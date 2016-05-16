package net.devintia.commons.bukkit.armorstand.commands;

import net.devintia.commons.bukkit.armorstand.ArmorStandModel;
import net.devintia.commons.bukkit.armorstand.ArmorStandModelHandler;
import net.devintia.commons.bukkit.command.CommandArguments;
import net.devintia.commons.bukkit.command.CommandUtil;
import net.devintia.commons.bukkit.command.CompleterInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author MiniDigger
 * @version 1.0.0
 */
public class ArmorStandModelCompleter {

    private ArmorStandModelHandler armorStandModelHandler;
    private Plugin plugin;

    public ArmorStandModelCompleter( ArmorStandModelHandler armorStandModelHandler, Plugin plugin ) {
        this.armorStandModelHandler = armorStandModelHandler;
        this.plugin = plugin;
    }

    @CompleterInfo( name = "loadmodel" )
    public List<String> loadModelCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            String[] names = plugin.getDataFolder().list( ( dir, name ) -> name.endsWith( ".model" ) );

            result.addAll( Arrays.asList( names ) );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }

    @CompleterInfo( name = "movemodel" )
    public List<String> moveModelCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            List<String> names = new ArrayList<>();

            for ( ArmorStandModel model : armorStandModelHandler.getModels() ) {
                names.add( model.getName() );
            }

            result.addAll( names );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }


    @CompleterInfo( name = "rotatemodel" )
    public List<String> rotateModelCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            List<String> names = new ArrayList<>();

            for ( ArmorStandModel model : armorStandModelHandler.getModels() ) {
                names.add( model.getName() );
            }

            result.addAll( names );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }

    @CompleterInfo( name = "followmodel" )
    public List<String> followmodelCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            List<String> names = new ArrayList<>();

            for ( ArmorStandModel model : armorStandModelHandler.getModels() ) {
                names.add( model.getName() );
            }

            result.addAll( names );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }

    @CompleterInfo( name = "ridemodel" )
    public List<String> ridemodelCompleter( CommandArguments args ) {
        final List<String> result = new ArrayList<>();

        if ( args.getArgs().length == 1 ) {
            List<String> names = new ArrayList<>();

            for ( ArmorStandModel model : armorStandModelHandler.getModels() ) {
                names.add( model.getName() );
            }

            result.addAll( names );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }
}
