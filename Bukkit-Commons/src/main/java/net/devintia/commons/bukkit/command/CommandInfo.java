package net.devintia.commons.bukkit.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simple annotation to represent a command<br>
 * Methods which are annotated with this @interface need to have single {@link CommandArguments}
 *
 * @author MiniDigger
 * @version 1.0.0
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface CommandInfo {

    /**
     * The name of the command. Can be a root level command or a sub command. Command levels are seperated using a '.'<br>
     * Examples:<br>
     * <code>
     * command.subcommand<br>
     * command<br>
     * command.subcommand.subsubcommand<br>
     * </code>
     *
     * @return The name of the command
     */
    String name();

    /**
     * The permission node a player needs to have attached for him to be able to execute this command
     *
     * @return The permission node
     */
    String perm();

    /**
     * The aliases for this command. The aliases follow the same naming convention listed in {@link #name()}
     *
     * @return The aliases for this command, default is an empty array
     */
    String[] aliases() default {};

    /**
     * Whether or not the console should be able to execute this command. By default, this returrns true.<br>
     * If possible, every command should be able to be executed by the console, only if the command uses attributes of the sending player this should be set to false
     *
     * @return Whether or not the console should be able to execute this command.
     */
    boolean allowConsole() default true;

    /**
     * The usage info gets printed if the sender failed to enter the correct arguments. < command> will be replaced with the used command label
     *
     * @return The usage info for this (sub)command
     */
    String usage() default "";

    /**
     * The description is used for the help command. It should be a small one liner to explain what this (sub)command does.
     *
     * @return The description of this (sub)command
     */
    String description() default "";
}
