package net.devintia.commons.bukkit.command;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Util methods related to the command system
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class CommandUtil {

    /**
     * Filters the list to only include entries that start with the prefix
     *
     * @param list   The list that should be filtered
     * @param prefix The prefix every entry should start with
     * @return The filtered list
     */
    public static List<String> filterTabCompletions( List<String> list, String prefix ) {
        final List<String> result = new ArrayList<>();

        for ( final String s : list ) {
            if ( s.toLowerCase().startsWith( prefix.toLowerCase() ) ) {
                result.add( s );
            }
        }

        return result;
    }

    public static void addOnlinePlayerNames( List<String> input ) {
        Bukkit.getOnlinePlayers().forEach( ( Player p ) -> input.add( p.getName() ) );
    }
}
