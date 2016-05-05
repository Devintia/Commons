package net.devintia.commons.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wrapper for {@link Command} to inject into the command map
 *
 * @author MiniDigger
 * @version 1.0.0
 */
class CommandWrapper extends Command {
    private final Plugin plugin;
    private CommandExecutor executor;
    private CompleterWrapper completer;

    /**
     * @param label    The label of this command
     * @param executor The executor to execute this command
     * @param plugin   The plugin that is providing this command
     */
    CommandWrapper( String label, CommandExecutor executor, Plugin plugin ) {
        super( label );
        checkNotNull( executor );
        checkNotNull( plugin );

        this.executor = executor;
        this.plugin = plugin;
        this.usageMessage = "";
    }

    @Override
    public boolean execute( CommandSender commandSender, String commandLabel, String[] args ) {
        checkNotNull( commandSender );
        checkNotNull( commandLabel );
        checkNotNull( args );

        boolean success;

        if ( !plugin.isEnabled() ) {
            return false;
        }

        if ( !testPermission( commandSender ) ) {
            return true;
        }

        //execute
        try {
            success = executor.onCommand( commandSender, this, commandLabel, args );
        } catch ( Throwable ex ) {
            throw new CommandException( "Unhandled exception executing command '" + commandLabel + "' in plugin "
                    + plugin.getDescription().getFullName(), ex );
        }

        // print usage
        if ( !success && usageMessage.length() > 0 ) {
            for ( String line : usageMessage.replace( "<command>", commandLabel ).split( "\n" ) ) {
                commandSender.sendMessage( line );
            }
        }

        return success;
    }

    @Override
    public List<String> tabComplete( CommandSender sender, String alias, String[] args )
            throws CommandException, IllegalArgumentException {
        checkNotNull( sender );
        checkNotNull( alias );
        checkNotNull( args );

        List<String> completions = null;
        try {
            // if we have a completer, get the completions from it
            if ( completer != null ) {
                completions = completer.onTabComplete( sender, this, alias, args );
            }
            // if not succeeded, try bukkits completer
            if ( completions == null && executor instanceof TabCompleter ) {
                completions = ( (TabCompleter) executor ).onTabComplete( sender, this, alias, args );
            }
        } catch ( Throwable ex ) {
            StringBuilder message = new StringBuilder();
            message.append( "Unhandled exception during tab completion for command '/" ).append( alias ).append( ' ' );
            for ( String arg : args ) {
                message.append( arg ).append( ' ' );
            }
            message.deleteCharAt( message.length() - 1 ).append( "' in plugin " )
                    .append( plugin.getDescription().getFullName() );
            throw new CommandException( message.toString(), ex );
        }

        if ( completions == null ) {
            return super.tabComplete( sender, alias, args );
        }
        return completions;
    }

    /**
     * @return The completer that handles tab completion for this (sub)command. May be null if not set (yet)
     */
    CompleterWrapper getCompleter() {
        return completer;
    }

    /**
     * Sets the completer that should handle tab completion for this (sub)command.
     *
     * @param completer The tab completer
     */
    void setCompleter( CompleterWrapper completer ) {
        checkNotNull( completer );
        this.completer = completer;
    }
}
