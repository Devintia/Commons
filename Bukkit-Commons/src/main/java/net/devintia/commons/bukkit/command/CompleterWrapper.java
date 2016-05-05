package net.devintia.commons.bukkit.command;

import lombok.extern.java.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Wrapper for {@link org.bukkit.command.TabCompleter}<br>
 * Supports multiple completers for one command (one completer per subcommand)
 *
 * @author MiniDigger
 * @version 1.0.0
 */
@Log
class CompleterWrapper implements TabCompleter {

    private Map<String, Entry<Method, Object>> completers = new HashMap<>();

    /**
     * Adds a new completer for the subcommand label to this completer<br>
     * Overrides the old one, if there was a completer for label already
     *
     * @param label          The label
     * @param executorMethod The method that should handle the completion request
     * @param executorObject The object the method belongs to
     */
    void addCompleter( String label, Method executorMethod, Object executorObject ) {
        completers.put( label, new AbstractMap.SimpleEntry<>( executorMethod, executorObject ) );
    }

    @Override
    public List<String> onTabComplete( CommandSender commandSender, Command command, String label, String[] args ) {
        // loop through all arguments backwards to find a registered command
        for ( int i = args.length; i >= 0; i-- ) {
            // build command name
            StringBuilder buffer = new StringBuilder();
            buffer.append( label.toLowerCase() );
            for ( int x = 0; x < i; x++ ) {
                if ( !args[x].equals( "" ) && !args[x].equals( " " ) ) {
                    buffer.append( "." ).append( args[x].toLowerCase() );
                }
            }
            String cmdLabel = buffer.toString();

            // if a completer exists, execute it
            if ( completers.containsKey( cmdLabel ) ) {
                // fix arguments
                int subCommand = cmdLabel.split( Pattern.quote( "." ) ).length - 1;
                String[] newArgs = new String[args.length - subCommand];
                System.arraycopy( args, subCommand, newArgs, 0, args.length - subCommand );

                Entry<Method, Object> entry = completers.get( cmdLabel );
                try {
                    Object result = entry.getKey().invoke( entry.getValue(), new CommandArguments( command, commandSender, newArgs ) );
                    if ( result instanceof List ) {
                        return (List<String>) result;
                    }
                    log.warning( "Could not handle tab completion for command " + cmdLabel + ": Returned object of executorMethod " + entry.getKey().getName() + " of class " + entry.getKey().getClass().getName() + " was not a list!" );
                    return new ArrayList<>();
                } catch ( IllegalArgumentException | IllegalAccessException | InvocationTargetException e ) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }
        }
        return null;
    }
}
