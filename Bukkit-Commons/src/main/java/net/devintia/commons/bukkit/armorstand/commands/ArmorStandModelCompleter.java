package net.devintia.commons.bukkit.armorstand.commands;

import net.devintia.commons.bukkit.TestPlugin;
import net.devintia.commons.bukkit.armorstand.ArmorStandModel;
import net.devintia.commons.bukkit.command.CommandArguments;
import net.devintia.commons.bukkit.command.CommandUtil;
import net.devintia.commons.bukkit.command.CompleterInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Martin on 05.06.2016.
 */
public class ArmorStandModelCompleter {

    private TestPlugin plugin;

    public ArmorStandModelCompleter( TestPlugin plugin ) {
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

            for ( ArmorStandModel model : plugin.getArmorStandModelHandler().getModels() ) {
                names.add( model.getName() );
            }

            result.addAll( names );

            return CommandUtil.filterTabCompletions( result, args.getArg( 0 ) );
        } else {
            return new ArrayList<>();
        }
    }
}
