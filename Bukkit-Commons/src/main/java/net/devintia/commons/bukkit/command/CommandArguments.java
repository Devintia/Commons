package net.devintia.commons.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds all information about a command that was excuted by a player
 *
 * @author MiniDigger
 * @version 1.0.0
 */
public class CommandArguments {

    private CommandSender sender;
    private Command command;
    private Player player;
    private String[] args;

    CommandArguments( Command command, CommandSender sender, String[] args ) {
        checkNotNull( command );
        checkNotNull( sender );
        checkNotNull( args );

        this.sender = sender;
        this.args = args;
        this.command = command;

        if ( sender instanceof Player ) {
            this.player = (Player) sender;
        }
    }

    /**
     * @return The {@link CommandSender} who executed this command
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * @return whether the command was executed by a player or not
     */
    public boolean hasPlayer() {
        return player != null;
    }

    /**
     * @return The {@link Player} who executed the command. Null if(!hasPlayer())
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @param i The index
     * @return The argument at index i
     */
    public String getArg( int i ) {
        checkArgument( i >= 0 && i < args.length );

        return args[i];
    }

    /**
     * @return The number of arguments the sender entered
     */
    public int getNumArgs() {
        return args.length;
    }

    /**
     * @return All arguments the sender entered
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * @return The original command
     */
    public Command getCommand() {
        return command;
    }
}
