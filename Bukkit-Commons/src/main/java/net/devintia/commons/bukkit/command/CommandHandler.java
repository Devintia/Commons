package net.devintia.commons.bukkit.command;


import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles all commands and thier completers for one plugin.<br>
 * Methods that should handle commands need to be annotated with {@link CommandInfo} and have a single {@link CommandArguments} parameter<br>
 * Methods that should handle command completions need to be annotated with {@link CompleterInfo} and have a single {@link CommandArguments} parameter and need to return a List with all completions
 *
 * @author MiniDigger
 * @version 1.0.0
 */
@Log
public class CommandHandler implements CommandExecutor {

    private CommandMap bukkitCommandMap;
    private Map<String, org.bukkit.command.Command> knownCommands;
    //<commandlabel, <executorMethod,executorObject>
    private Map<String, Entry<Method, Object>> commandMap;
    private Plugin plugin;

    /**
     * Initialises this CommandHandler. Tries to get a reference to bukkits command map to inject our commands
     *
     * @param plugin The plugin that is the owner of this CommandHandler instance
     */
    public CommandHandler( Plugin plugin ) {
        checkNotNull( plugin );

        this.plugin = plugin;
        this.commandMap = new HashMap<>();

        if ( plugin.getServer().getPluginManager() instanceof SimplePluginManager ) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();
            try {
                Field field = SimplePluginManager.class.getDeclaredField( "commandMap" );
                field.setAccessible( true );
                bukkitCommandMap = (CommandMap) field.get( manager );
            } catch ( IllegalAccessException | NoSuchFieldException e ) {
                log.severe( "Could get commandMap from the SimplePluginManager, can't register any commands!" );
                e.printStackTrace();
                return;
            }

            try {
                final Field knownCommandField = SimpleCommandMap.class.getDeclaredField( "knownCommands" );
                knownCommandField.setAccessible( true );
                Object obj = knownCommandField.get( bukkitCommandMap );
                if ( obj instanceof Map ) {
                    knownCommands = (Map<String, org.bukkit.command.Command>) knownCommandField.get( bukkitCommandMap );
                } else {
                    log.warning( "Could get knownCommands from the SimpleCommandMap (returned unexpected object), can't unregister any commands!" );
                }
            } catch ( NoSuchFieldException | IllegalAccessException e ) {
                log.warning( "Could get knownCommands from the SimpleCommandMap, can't unregister any commands!" );
                e.printStackTrace();
            }

        } else {
            // should never occur
            throw new IllegalArgumentException( "Specified plugin has no SimplePluginManager?!" );
        }
    }

    /**
     * Unregisters all valid commands and completer in that class
     *
     * @param executorObject The object which contains command and completer methods
     */
    public void unregister( Object executorObject ) {
        checkNotNull( executorObject );

        for ( Method method : executorObject.getClass().getMethods() ) {
            if ( method.isAnnotationPresent( CommandInfo.class ) ) {
                CommandInfo commandInfo = method.getAnnotation( CommandInfo.class );

                if ( method.getParameterCount() != 1 || !method.getParameterTypes()[0].equals( CommandArguments.class ) ) {
                    log.warning( "Could not unregister command " + method.getName() + " in class " + executorObject.getClass().getName() + ": Method may only have a single CommandArguments parameter!" );
                    continue;
                }

                unregisterCommand( commandInfo.name() );
                for ( String alias : commandInfo.aliases() ) {
                    unregisterCommand( alias );
                }
            }
        }
    }

    @Override
    public boolean onCommand( CommandSender commandSender, Command command, String label, String[] args ) {
        checkNotNull( commandSender );
        checkNotNull( command );
        checkNotNull( label );
        checkNotNull( args );

        // loop through all arguments backwards to find a registered command
        for ( int i = args.length; i >= 0; i-- ) {
            // build command name
            StringBuilder buffer = new StringBuilder();
            buffer.append( label.toLowerCase() );
            for ( int x = 0; x < i; x++ ) {
                buffer.append( "." ).append( args[x].toLowerCase() );
            }
            String cmdLabel = buffer.toString();

            // if command exists, execute it
            if ( commandMap.containsKey( cmdLabel ) ) {
                Method executorMethod = commandMap.get( cmdLabel ).getKey();
                Object executorObject = commandMap.get( cmdLabel ).getValue();
                CommandInfo commandInfo = executorMethod.getAnnotation( CommandInfo.class );
                if ( !commandInfo.perm().equals( "" ) && !commandSender.hasPermission( commandInfo.perm() ) ) {
                    //TODO Send no permission message
                    return true;
                }
                if ( commandInfo.allowConsole() && !( commandSender instanceof Player ) ) {
                    //TODO Send no console message
                    return true;
                }
                try {
                    // fix arguments
                    int subCommand = cmdLabel.split( Pattern.quote( "." ) ).length - 1;
                    String[] newArgs = new String[args.length - subCommand];
                    System.arraycopy( args, subCommand, newArgs, 0, args.length - subCommand );

                    executorMethod.invoke( executorObject, new CommandArguments( command, commandSender, newArgs ) );
                } catch ( IllegalArgumentException | InvocationTargetException | IllegalAccessException e ) {
                    e.printStackTrace();
                }
                return true;
            }
        }

        log.warning( "Command " + label + " is not handled!" );
        return false;
    }


    /**
     * Registers all valid commands and completer in that class
     *
     * @param executorObject The object which contains command and completer methods
     */

    public void register( Object executorObject ) {
        checkNotNull( executorObject );

        for ( Method method : executorObject.getClass().getMethods() ) {
            if ( method.isAnnotationPresent( CommandInfo.class ) ) {
                CommandInfo commandInfo = method.getAnnotation( CommandInfo.class );

                if ( method.getParameterCount() != 1 || !method.getParameterTypes()[0].equals( CommandArguments.class ) ) {
                    log.warning( "Could not register command " + method.getName() + " in class " + executorObject.getClass().getName() + ": Method may only have a single CommandArguments parameter!" );
                    continue;
                }

                registerCommand( commandInfo.name(), commandInfo.description(), commandInfo.usage(), method, executorObject );
                for ( String alias : commandInfo.aliases() ) {
                    registerCommand( alias, commandInfo.description(), commandInfo.usage(), method, executorObject );
                }
            } else if ( method.isAnnotationPresent( CompleterInfo.class ) ) {
                CompleterInfo completerInfo = method.getAnnotation( CompleterInfo.class );

                if ( method.getParameterCount() != 1 || !method.getParameterTypes()[0].equals( CommandArguments.class ) ) {
                    log.warning( "Could not register completer " + method.getName() + " in class " + executorObject.getClass().getName() + ": Method may only have a single CommandArguments parameter!" );
                    continue;
                }

                if ( method.getReturnType() != List.class ) {
                    log.warning( "Could not register completer " + method.getName() + " in class " + executorObject.getClass().getName() + ": Method needs to return a List!" );
                    continue;
                }

                registerCompleter( completerInfo.name(), method, executorObject );
                for ( String alias : completerInfo.aliases() ) {
                    registerCompleter( alias, method, executorObject );
                }
            }
        }
    }

    /**
     * Registers the registered commands into the bukkit help map
     */
    public void registerHelp() {
        Set<HelpTopic> help = new TreeSet<>( HelpTopicComparator.helpTopicComparatorInstance() );
        for ( String commandLabel : commandMap.keySet() ) {
            if ( !commandLabel.contains( "." ) ) {
                org.bukkit.command.Command cmd = bukkitCommandMap.getCommand( commandLabel );
                HelpTopic topic = new GenericCommandHelpTopic( cmd );
                String perm = commandMap.get( commandLabel ).getKey().getAnnotation( CommandInfo.class ).perm();
                topic.amendCanSee( perm + ".help" );
                help.add( topic );
            }
        }
        IndexHelpTopic topic = new IndexHelpTopic( plugin.getName(), "All commands for " + plugin.getName(), plugin.getName() + ".help", help,
                "Below is a list of all " + plugin.getName() + " commands:" );
        Bukkit.getServer().getHelpMap().addTopic( topic );
        help.forEach( ( t ) -> Bukkit.getServer().getHelpMap().addTopic( t ) );
    }

    private void registerCommand( String commandLabel, String description, String usage, Method executorMethod, Object executorObject ) {
        commandMap.put( commandLabel.toLowerCase(), new AbstractMap.SimpleEntry<>( executorMethod, executorObject ) );
        commandMap.put( this.plugin.getName() + ':' + commandLabel.toLowerCase(), new AbstractMap.SimpleEntry<>( executorMethod, executorObject ) );

        String label = commandLabel.split( Pattern.quote( "." ) )[0].toLowerCase();
        Command command = bukkitCommandMap.getCommand( label );
        if ( command == null ) {
            command = new CommandWrapper( label, this, plugin );
            bukkitCommandMap.register( plugin.getName(), command );
        }

        // set description if root command
        if ( command.getDescription().equals( "" ) && label.equalsIgnoreCase( commandLabel ) ) {
            command.setDescription( description );
        }

        // set usage if root command
        if ( command.getUsage().equals( "" ) && label.equalsIgnoreCase( commandLabel ) ) {
            command.setUsage( usage );
        }
    }

    private void registerCompleter( String commandLabel, Method executorMethod, Object executorObject ) {
        String cmdLabel = commandLabel.split( Pattern.quote( "." ) )[0].toLowerCase();

        Command command = bukkitCommandMap.getCommand( commandLabel );
        // if command not registered, register it
        if ( command == null ) {
            command = new CommandWrapper( cmdLabel, this, plugin );
            bukkitCommandMap.register( plugin.getName(), command );
        }
        // if command was registered by us, simple add the completer
        if ( command instanceof CommandWrapper ) {
            CommandWrapper commandWrapper = (CommandWrapper) bukkitCommandMap.getCommand( cmdLabel );
            if ( commandWrapper.getCompleter() == null ) {
                commandWrapper.setCompleter( new CompleterWrapper() );
            }
            commandWrapper.getCompleter().addCompleter( commandLabel, executorMethod, executorObject );
        } else
            //else we need to hack into bukkits completers
            if ( bukkitCommandMap.getCommand( cmdLabel ) instanceof PluginCommand ) {
                try {
                    Object bukkitCommand = bukkitCommandMap.getCommand( cmdLabel );
                    Field field = bukkitCommand.getClass().getDeclaredField( "completer" );
                    field.setAccessible( true );
                    if ( field.get( bukkitCommand ) == null ) {
                        CompleterWrapper completer = new CompleterWrapper();
                        completer.addCompleter( commandLabel, executorMethod, executorObject );
                        field.set( bukkitCommand, completer );
                    } else if ( field.get( bukkitCommand ) instanceof CompleterWrapper ) {
                        CompleterWrapper completer = (CompleterWrapper) field.get( bukkitCommand );
                        completer.addCompleter( commandLabel, executorMethod, executorObject );
                    } else {
                        log.warning( "Unable to register tab completer " + executorMethod.getName()
                                + ". A tab completer is already registered for that command!" );
                    }
                } catch ( Exception ex ) {
                    log.warning( "Error while registering completer" );
                    ex.printStackTrace();
                }
            }
    }

    /**
     * Completely unregisters a command. This can only unregister root level commands, all sub commands get unregistered too.
     * If you whish to update the help page, you need to do that manually!
     *
     * @param commandLabel The commandLabel to unregister
     */
    public void unregisterCommand( String commandLabel ) {
        checkNotNull( commandLabel );

        commandMap.remove( commandLabel.toLowerCase() );
        commandMap.remove( this.plugin.getName() + ':' + commandLabel.toLowerCase() );

        String label = commandLabel.split( Pattern.quote( "." ) )[0].toLowerCase();
        Command command = bukkitCommandMap.getCommand( label );
        if ( command != null ) {
            command.unregister( bukkitCommandMap );

            for ( final Map.Entry<String, org.bukkit.command.Command> entry : knownCommands.entrySet() ) {
                if ( entry.getKey().equals( commandLabel ) ) {
                    entry.getValue().unregister( bukkitCommandMap );
                }
            }
            knownCommands.remove( commandLabel );
        }
    }
}
